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
import java.nio.file.Files;
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

    private void validateExtension(MultipartFile file, String targetFormat) {
        String originalExt = getFileExtension(Objects.requireNonNull(file.getOriginalFilename()));
        DocumentFormat sourceFormat = DocumentFormat.fromExtension(originalExt)
                .orElseThrow(() -> new BadRequestException("Định dạng gốc không được hỗ trợ"));

        DocumentFormat target = DocumentFormat.fromExtension(targetFormat)
                .orElseThrow(() -> new BadRequestException("Định dạng đích không được hỗ trợ"));

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
        validateExtension(file, targetFormat);

        File tempFile = null;
        File outputFile = null;
        try {
            // Lưu file gốc tạm thời
            tempFile = new File(tempDir + file.getOriginalFilename());
            file.transferTo(tempFile);

            // Tạo tên file đầu ra
            String baseName = file.getOriginalFilename().substring(0, file.getOriginalFilename().lastIndexOf("."));
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

    // Xóa file tạm sau khi xử lý xong
    @Override
    public void deleteFileIfExists(File file) {
        if (file != null && file.exists()) {
            file.delete();
        }
    }


}
