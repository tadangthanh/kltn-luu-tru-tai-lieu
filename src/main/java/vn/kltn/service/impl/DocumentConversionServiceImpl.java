package vn.kltn.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.kltn.common.DocumentFormat;
import vn.kltn.entity.Document;
import vn.kltn.exception.*;
import vn.kltn.service.IAzureStorageService;
import vn.kltn.service.IDocumentConversionService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

import static com.google.common.io.Files.getFileExtension;
import static vn.kltn.common.DocumentFormat.SUPPORTED_CONVERSIONS;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "DOCUMENT_CONVERSION_SERVICE")
public class DocumentConversionServiceImpl implements IDocumentConversionService {
    @Value("${app.conversion.temp-dir}")
    private String tempDir;
    private final IAzureStorageService azureStorageService;
    @Qualifier("taskExecutor")
    private final Executor taskExecutor;

    private void validateExtension(File file, String targetFormat) {
        String originalExt = getFileExtension(Objects.requireNonNull(file.getName()));
        DocumentFormat sourceFormat = DocumentFormat.fromExtension(originalExt).orElseThrow(() -> new UnsupportedFileFormatException("Định dạng gốc không được hỗ trợ"));

        DocumentFormat target = DocumentFormat.fromExtension(targetFormat).orElseThrow(() -> new UnsupportedFileFormatException("Định dạng đích không được hỗ trợ"));
        if (sourceFormat.getExtension().equals(target.getExtension())) {
            log.warn("Định dạng phải khác nhau");
            throw new InvalidDataException("Không hỗ trợ chuyển đổi từ PDF sang WORD");
        }
        if (!SUPPORTED_CONVERSIONS.containsKey(sourceFormat) || !SUPPORTED_CONVERSIONS.get(sourceFormat).contains(target)) {
            throw new BadRequestException("Chuyển đổi từ " + sourceFormat.getExtension() + " sang " + target.getExtension() + " không được hỗ trợ.");
        }
    }

    @Override
    public String convertFile(MultipartFile file, String targetFormat) {
        if (file.isEmpty()) {
            log.warn("file is empty");
            throw new ResourceNotFoundException("Không tìm thấy file");
        }

        File tempFile = null;
        File outputFile = null;
        try {
            // Lưu file gốc tạm thời
            tempFile = new File(tempDir + file.getOriginalFilename());
            file.transferTo(tempFile);
            validateExtension(tempFile, targetFormat);


            // Tạo tên file đầu ra
            String baseName = Objects.requireNonNull(file.getOriginalFilename()).substring(0, file.getOriginalFilename().lastIndexOf("."));
            String outputFilePath = tempFile.getParent() + File.separator + baseName + "." + targetFormat;
            outputFile = new File(outputFilePath);

            // Gọi LibreOffice để chuyển đổi
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "soffice",
                    "--headless",
                    "--convert-to",
                    targetFormat,
                    "--outdir",
                    tempFile.getParent(),
                    tempFile.getAbsolutePath());

            Process process = processBuilder.start();
            int exitValue = process.waitFor();
            if (exitValue != 0 || !outputFile.exists()) {
                log.error("Chuyển đổi thất bại hoặc không tạo được file đầu ra.");
                throw new BadRequestException("Có lỗi xảy ra trong quá trình chuyển đổi định dạng");
            }
            // Upload lên Azure
            try (InputStream inputStream = new FileInputStream(outputFile)) {
                return azureStorageService.uploadChunkedWithContainerDefault(inputStream, outputFile.getName(), outputFile.length(), 10 * 1024 * 1024);
            }

        } catch (IOException | InterruptedException e) {
            log.error("Lỗi chuyển đổi: {}", e.getMessage());
            throw new BadRequestException("Có lỗi xảy ra trong quá trình chuyển đổi định dạng");
        } finally {
            log.info("finish convert file");
            deleteFileIfExists(tempFile);
            deleteFileIfExists(outputFile);
        }
    }

    @Override
    public String convertStoredFile(String blobName, String targetFormat) {
        File downloadedFile = null;
        File convertedFile = null;

        try {
            // 1. Tải file từ Azure Blob về
            downloadedFile = azureStorageService.downloadToFile(blobName, tempDir);
            if (downloadedFile == null || !downloadedFile.exists()) {
                throw new BadRequestException("Không thể tải file từ Azure Blob");
            }
            validateExtension(downloadedFile, targetFormat);

            // 2. Xác định tên file đầu ra
            String newFileName = replaceExtension(downloadedFile.getName(), targetFormat);
            String outputFilePath = downloadedFile.getParent() + File.separator + newFileName;
            convertedFile = new File(outputFilePath);

            // 3. Gọi LibreOffice để chuyển đổi
            ProcessBuilder pb = new ProcessBuilder(
                    "soffice",
                    "--headless",
                    "--convert-to",
                    targetFormat,
                    "--outdir",
                    downloadedFile.getParent(),
                    downloadedFile.getAbsolutePath());
            Process process = pb.start();
            int exitCode = process.waitFor();
            if (exitCode != 0 || !convertedFile.exists()) {
                throw new ConversionException("Chuyển đổi định dạng thất bại");
            }

            // 4. Upload lại file đã chuyển đổi
            try (InputStream convertedStream = new FileInputStream(convertedFile)) {
                return azureStorageService.uploadChunkedWithContainerDefault(convertedStream,
                        convertedFile.getName(), convertedFile.length(), 10 * 1024 * 1024);
            }

        } catch (IOException | InterruptedException e) {
            log.error("Lỗi khi chuyển đổi file từ blob: {}", e.getMessage());
            throw new ConversionException("Chuyển đổi file thất bại: " + e.getMessage());
        } finally {
            deleteFileIfExists(downloadedFile);
            deleteFileIfExists(convertedFile);
        }
    }

    /***
     * chuyển đổi 1 blob trên azure thành nhiều ảnh dựa theo pages truyền vào, và lưu lại ảnh trên azure
     * @param document: document cần chuyển đổi sang hình ảnh preview
     * @param pages: các trang cần chuyển đổi thành ảnh, ví dụ: "1,5,7" (trang 1,5,7)
     * @return : danh sách các blobName img đã được lưu trên azure cloud
     */
    @Override
    public List<String> convertPdfToImagesAndUpload(Document document, List<Integer> pages) {
        List<String> uploadedImageBlobs = new ArrayList<>();
        File blobFile = null;

        try {
            // 1. Tải file PDF từ Azure Blob Storage
            blobFile = azureStorageService.downloadToFile(document.getBlobName(), tempDir);
            if (!blobFile.getName().toLowerCase().endsWith(".pdf")) {
                blobFile = convertToPdf(blobFile);
            }

            // 2. Chuyển đổi thành ảnh
            File[] imageFiles = convertPdfToImages(blobFile, pages);

            // 3. Upload bất đồng bộ từng ảnh
            List<CompletableFuture<String>> uploadFutures = Arrays.stream(imageFiles)
                    .map(imageFile -> CompletableFuture.supplyAsync(() -> {
                        try {
                            return uploadImageToBlob(imageFile);
                        } catch (IOException e) {
                            log.error("Lỗi khi upload ảnh: {}", e.getMessage());
                            throw new BadRequestException("Lỗi khi upload ảnh: " + e.getMessage());
                        } finally {
                            log.info("finish convert file");
                            deleteFileIfExists(imageFile);
                        }
                    }, taskExecutor))
                    .toList();

            // 4. Chờ tất cả upload hoàn tất
            CompletableFuture.allOf(uploadFutures.toArray(new CompletableFuture[0])).join();

            // 5. Lấy kết quả upload
            for (CompletableFuture<String> future : uploadFutures) {
                uploadedImageBlobs.add(future.get());
            }

        } catch (IOException | InterruptedException | ExecutionException e) {
            log.error("Lỗi khi chuyển đổi PDF sang ảnh và upload", e);
            throw new ConversionException("Lỗi khi chuyển đổi hoặc upload hình ảnh");
        } finally {
            deleteFileIfExists(blobFile);
        }

        return uploadedImageBlobs;
    }

    @Override
    public File convertToPdf(File file) {
        File tempFile = null;
        File outputFile;
        try {
            // Lưu file gốc tạm thời
            tempFile = new File(tempDir + file.getName());
            Files.copy(file.toPath(), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            // Validate không phải PDF, nếu là PDF thì không cần convert
            validateExtension(tempFile, "pdf");

            // Tạo tên file đầu ra
            String baseName = Objects.requireNonNull(file.getName()).substring(0, file.getName().lastIndexOf("."));
            String outputFilePath = tempFile.getParent() + File.separator + baseName + ".pdf";
            outputFile = new File(outputFilePath);

            // Gọi LibreOffice để chuyển đổi
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "soffice",
                    "--headless",
                    "--convert-to", "pdf",
                    "--outdir", tempFile.getParent(),
                    tempFile.getAbsolutePath()
            );

            Process process = processBuilder.start();
            int exitValue = process.waitFor();
            if (exitValue != 0 || !outputFile.exists()) {
                log.error("Chuyển đổi thất bại hoặc không tạo được file đầu ra.");
                throw new ConversionException("Có lỗi xảy ra trong quá trình chuyển đổi định dạng");
            }

            return outputFile;

        } catch (IOException | InterruptedException e) {
            log.error("Lỗi chuyển đổi: {}", e.getMessage());
            throw new ConversionException("Có lỗi xảy ra trong quá trình chuyển đổi định dạng");
        } finally {
            log.info("Hoàn tất chuyển đổi file.");
            deleteFileIfExists(tempFile);       //  XÓA file gốc
        }
    }


    /**
     * Chuyển đổi PDF thành các ảnh cho các trang cụ thể
     */
    private File[] convertPdfToImages(File pdfFile, List<Integer> pages) throws IOException, InterruptedException {
        List<File> imageFiles = new ArrayList<>();
        String watermarkText = "abc@example.com"; // ← watermark email

        for (Integer page : pages) {
            String pdfFilePath = pdfFile.getAbsolutePath();

            // Tên file ảnh tạm
            String imageFilePath = tempDir + "output_page-" + String.format("%03d", page) + "_" + System.currentTimeMillis() + ".png";

            // Tạo lệnh với watermark
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "magick",
                    "-density", "300",
                    pdfFilePath + "[" + (page - 1) + "]", // trừ 1 vì page bắt đầu từ 0 trong ImageMagick
                    "-quality", "100",
                    "-fill", "rgba(0,0,0,0.4)", // watermark màu đen, mờ 50%
                    "-gravity", "center",
                    "-pointsize", "40",
                    "-annotate", "45", watermarkText,
                    imageFilePath
            );

            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new IOException("Lỗi khi chuyển đổi PDF thành ảnh cho trang: " + page);
            }

            File imageFile = new File(imageFilePath);
            if (imageFile.exists()) {
                imageFiles.add(imageFile);
            }
        }

        return imageFiles.toArray(new File[0]);
    }


    /**
     * Tải tệp ảnh lên Azure Blob Storage
     */
    private String uploadImageToBlob(File imageFile) throws IOException {
        // Tạo stream từ ảnh
        try (InputStream imageStream = new FileInputStream(imageFile)) {
            // Upload ảnh lên Azure Blob Storage
            return azureStorageService.uploadChunkedWithContainerDefault(imageStream, imageFile.getName(), imageFile.length(), 10 * 1024 * 1024  // Đặt kích thước chunk 10MB
            );
        }
    }


    private String replaceExtension(String fileName, String newExt) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot == -1) return fileName + "." + newExt;
        return fileName.substring(0, lastDot) + "." + newExt;
    }

    // Xóa file tạm sau khi xử lý xong
    @Override
    public void deleteFileIfExists(File file) {
        if (file != null && file.exists()) {
            log.info("Deleting file: {}", file.getName());
            // Compliant: result of file deletion is checked.
            if (!file.delete()) {
                throw new BadRequestException("Failed to delete the file!");
            }
        }
    }


}
