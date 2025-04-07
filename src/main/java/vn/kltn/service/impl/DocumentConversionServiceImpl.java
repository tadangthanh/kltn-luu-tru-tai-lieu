package vn.kltn.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.kltn.exception.BadRequestException;
import vn.kltn.service.IDocumentConversionService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "DOCUMENT_CONVERSION_SERVICE")
public class DocumentConversionServiceImpl implements IDocumentConversionService {
    @Value("${app.conversion.temp-dir}")
    private String tempDir;

    // Hàm chuyển đổi file Word thành PDF
    public File convertWordToPdf(File wordFile) throws IOException {
        String outputFilePath = wordFile.getParent() + File.separator + wordFile.getName().replace(".docx", ".pdf");
        File pdfFile = new File(outputFilePath);

        // Gọi lệnh LibreOffice
        ProcessBuilder processBuilder = new ProcessBuilder(
                "soffice",
                "--headless",
                "--convert-to", "pdf",
                "--outdir", wordFile.getParent(),
                wordFile.getAbsolutePath()
        );

        Process process = processBuilder.start();
        try {
            int exitValue = process.waitFor(); // Chờ quá trình hoàn tất
            if (exitValue != 0) {
                throw new BadRequestException("Có lỗi xảy ra trong quá trình chuyển đổi định dạng");
            }
        } catch (InterruptedException e) {
            log.error(e.getMessage());
            throw new BadRequestException("Có lỗi xảy ra trong quá trình chuyển đổi định dạng");
        }

        if (!pdfFile.exists()) {
            log.error("File PDF không được tạo ra.");
            throw new BadRequestException("Có lỗi xảy ra trong quá trình chuyển đổi định dạng");
        }

        return pdfFile;
    }

    // Đọc nội dung file PDF và trả về dưới dạng byte array
    @Override
    public byte[] readPdfFileAsBytes(File pdfFile) throws IOException {
        return Files.readAllBytes(pdfFile.toPath());
    }

    // Xóa file tạm sau khi xử lý xong
    @Override
    public void deleteFileIfExists(File file) {
        if (file != null && file.exists()) {
            file.delete();
        }
    }


}
