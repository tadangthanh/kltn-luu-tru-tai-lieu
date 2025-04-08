package vn.kltn.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.kltn.common.DocumentFormat;
import vn.kltn.exception.BadRequestException;
import vn.kltn.exception.InvalidDataException;
import vn.kltn.service.IAzureStorageService;
import vn.kltn.service.IDocumentConversionService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.google.common.io.Files.getFileExtension;
import static vn.kltn.common.DocumentFormat.SUPPORTED_CONVERSIONS;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "DOCUMENT_CONVERSION_SERVICE")
public class DocumentConversionServiceImpl implements IDocumentConversionService {
    @Value("${app.conversion.temp-dir}")
    private String tempDir;
    private final IAzureStorageService azureStorageService;

    private void validateExtension(File file, String targetFormat) {
        String originalExt = getFileExtension(Objects.requireNonNull(file.getName()));
        DocumentFormat sourceFormat = DocumentFormat.fromExtension(originalExt).orElseThrow(() -> new BadRequestException("Định dạng gốc không được hỗ trợ"));

        DocumentFormat target = DocumentFormat.fromExtension(targetFormat).orElseThrow(() -> new BadRequestException("Định dạng đích không được hỗ trợ"));
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
            throw new InvalidDataException("Không tìm thấy file");
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
            try (InputStream outputStream = new FileInputStream(outputFile)) {
                return azureStorageService.uploadChunkedWithContainerDefault(outputStream, outputFile.getName(), outputFile.length(), 10 * 1024 * 1024);
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
            ProcessBuilder pb = new ProcessBuilder("soffice", "--headless", "--convert-to", targetFormat, "--outdir", downloadedFile.getParent(), downloadedFile.getAbsolutePath());
            Process process = pb.start();
            int exitCode = process.waitFor();
            if (exitCode != 0 || !convertedFile.exists()) {
                throw new BadRequestException("Chuyển đổi định dạng thất bại");
            }

            // 4. Upload lại file đã chuyển đổi
            try (InputStream convertedStream = new FileInputStream(convertedFile)) {
                return azureStorageService.uploadChunkedWithContainerDefault(convertedStream, convertedFile.getName(), convertedFile.length(), 10 * 1024 * 1024);
            }

        } catch (IOException | InterruptedException e) {
            log.error("Lỗi khi chuyển đổi file từ blob: {}", e.getMessage());
            throw new BadRequestException("Chuyển đổi file thất bại: " + e.getMessage());
        } finally {
            deleteFileIfExists(downloadedFile);
            deleteFileIfExists(convertedFile);
        }
    }

    /***
     * chuyển đổi 1 blob trên azure thành nhiều ảnh dựa theo pages truyền vào, và lưu lại ảnh trên azure
     * @param blobName: tên blob trên azure
     * @param pages: các trang cần chuyển đổi thành ảnh, ví dụ: "1,5,7" (trang 1,5,7)
     * @return : danh sách các blobName img đã được lưu trên azure cloud
     */
    @Override
    public List<String> convertPdfToImagesAndUpload(String blobName, String pages) {
        List<String> uploadedImageBlobs = new ArrayList<>();
        File pdfFile = null;

        try {
            // 1. Tải tệp PDF từ Azure Blob Storage
            pdfFile = azureStorageService.downloadToFile(blobName, tempDir);

            // 2. Chuyển đổi PDF thành các ảnh (PNG)
            File[] imageFiles = convertPdfToImages(pdfFile, pages);

            // 3. Tải các ảnh lên Azure Blob Storage
            for (File imageFile : imageFiles) {
                String uploadedImageBlob = uploadImageToBlob(imageFile);
                uploadedImageBlobs.add(uploadedImageBlob);
                deleteFileIfExists(imageFile);  // Xóa ảnh sau khi upload
            }

        } catch (IOException | InterruptedException e) {
            log.error("Có lỗi xảy ra trong quá trình chuyển đổi PDF thành hình ảnh", e);
            throw new BadRequestException("Có lỗi trong quá trình chuyển đổi PDF thành hình ảnh");
        } finally {
            // Xóa tệp PDF sau khi xử lý xong
            deleteFileIfExists(pdfFile);
        }

        return uploadedImageBlobs;
    }


    /**
     * Chuyển đổi PDF thành các ảnh cho các trang cụ thể
     */
    private File[] convertPdfToImages(File pdfFile, String pages) throws IOException, InterruptedException {
        List<File> imageFiles = new ArrayList<>();
        // Chia các trang được chỉ định thành mảng (ví dụ: "1,3,5" => ["0", "2", "4"])
        String[] pagesArray = pages.split(",");
        // Lặp qua các trang và chuyển đổi từng trang một
        for (String page : pagesArray) {
            // Tạo đường dẫn tạm thời cho các tệp ảnh
            String pdfFilePath = pdfFile.getAbsolutePath();
            String imageFilePattern = tempDir + "output_page-" + String.format("%03d", Integer.parseInt(page) + 1) + System.currentTimeMillis() + ".png";  // Đặt tên ảnh
            // Sử dụng ImageMagick để chuyển đổi từng trang
            ProcessBuilder processBuilder = new ProcessBuilder("magick", "-density", "300",  // Đặt độ phân giải
                    pdfFilePath + "[" + page + "]",  // Chỉ định trang cụ thể
                    imageFilePattern);
            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new IOException("Lỗi khi chuyển đổi PDF thành ảnh cho trang: " + page);
            }
            // Thêm ảnh đã tạo vào danh sách
            File imageFile = new File(imageFilePattern);
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
