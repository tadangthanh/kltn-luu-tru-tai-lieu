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
        DocumentFormat sourceFormat = DocumentFormat.fromExtension(originalExt)
                .orElseThrow(() -> new BadRequestException("Định dạng gốc không được hỗ trợ"));

        DocumentFormat target = DocumentFormat.fromExtension(targetFormat)
                .orElseThrow(() -> new BadRequestException("Định dạng đích không được hỗ trợ"));
        if (sourceFormat.getExtension().equals(target.getExtension())) {
            log.warn("Định dạng phải khác nhau");
            throw new InvalidDataException("Không hỗ trợ chuyển đổi từ PDF sang WORD");
        }
        if (!SUPPORTED_CONVERSIONS.containsKey(sourceFormat) ||
            !SUPPORTED_CONVERSIONS.get(sourceFormat).contains(target)) {
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
                    "--convert-to", targetFormat,
                    "--outdir", tempFile.getParent(),
                    tempFile.getAbsolutePath()
            );

            Process process = processBuilder.start();
            int exitValue = process.waitFor();
            if (exitValue != 0 || !outputFile.exists()) {
                log.error("Chuyển đổi thất bại hoặc không tạo được file đầu ra.");
                throw new BadRequestException("Có lỗi xảy ra trong quá trình chuyển đổi định dạng");
            }
            // Upload lên Azure
            try (InputStream outputStream = new FileInputStream(outputFile)) {
                return azureStorageService.uploadChunkedWithContainerDefault(
                        outputStream,
                        outputFile.getName(),
                        outputFile.length(),
                        10 * 1024 * 1024
                );
            }

        } catch (IOException | InterruptedException e) {
            log.error("Lỗi chuyển đổi: {}", e.getMessage());
            throw new BadRequestException("Có lỗi xảy ra trong quá trình chuyển đổi định dạng");
        } finally {
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
                    "--convert-to", targetFormat,
                    "--outdir", downloadedFile.getParent(),
                    downloadedFile.getAbsolutePath()
            );
            Process process = pb.start();
            int exitCode = process.waitFor();
            if (exitCode != 0 || !convertedFile.exists()) {
                throw new BadRequestException("Chuyển đổi định dạng thất bại");
            }

            // 4. Upload lại file đã chuyển đổi
            try (InputStream convertedStream = new FileInputStream(convertedFile)) {
                return azureStorageService.uploadChunkedWithContainerDefault(
                        convertedStream,
                        convertedFile.getName(),
                        convertedFile.length(),
                        10 * 1024 * 1024
                );
            }

        } catch (IOException | InterruptedException e) {
            log.error("Lỗi khi chuyển đổi file từ blob: {}", e.getMessage());
            throw new BadRequestException("Chuyển đổi file thất bại: " + e.getMessage());
        } finally {
            deleteFileIfExists(downloadedFile);
            deleteFileIfExists(convertedFile);
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
            file.delete();
        }
    }


}
