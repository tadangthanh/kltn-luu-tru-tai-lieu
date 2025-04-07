package vn.kltn.controller.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import vn.kltn.service.IDocumentConversionService;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/v1/document-conversion")
@RequiredArgsConstructor
@Slf4j(topic = "DOCUMENT_CONVERSION_REST")
public class DocumentConversionRest {
    private final IDocumentConversionService documentConversionService;
    // Đường dẫn nơi lưu trữ file tạm thời trên server
    private static final String TEMP_DIR = "D:\\export\\temp\\";

    @PostMapping("/word-to-html")
    public String convertUsingPandoc(@RequestParam("file") MultipartFile file) {
        try {
            // Tạo file tạm để lưu file Word
            Path tempDocx = Files.createTempFile("upload-", ".docx");
            Files.write(tempDocx, file.getBytes());

            // Tạo file tạm để lưu HTML output
            Path tempHtml = Files.createTempFile("output-", ".html");

            // Gọi lệnh Pandoc để convert DOCX -> HTML
            ProcessBuilder pb = new ProcessBuilder(
                    "pandoc",
                    tempDocx.toAbsolutePath().toString(),
                    "-f", "docx",
                    "-t", "html",
                    "-o", tempHtml.toAbsolutePath().toString()
            );

            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                return "Pandoc lỗi khi chuyển đổi. Mã lỗi: " + exitCode;
            }

            // Đọc kết quả HTML trả về
            String html = Files.readString(tempHtml);

            // Xóa file tạm nếu cần
            Files.deleteIfExists(tempDocx);
            Files.deleteIfExists(tempHtml);

            return html;

        } catch (Exception e) {
            log.error(e.getMessage());
            return "Lỗi xử lý: " + e.getMessage();
        }
    }

    @PostMapping("/word-to-pdf")
    public ResponseEntity<?> convertWordToPdf(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Không có file nào được upload");
        }

        File tempFile = null;
        File pdfFile = null;
        try {
            // Lưu file Word tạm thời vào TEMP_DIR
            tempFile = new File(TEMP_DIR + file.getOriginalFilename());
            file.transferTo(tempFile);

            // Chuyển đổi sang PDF
            pdfFile = documentConversionService.convertWordToPdf(tempFile);

            // Đọc nội dung file PDF dưới dạng byte array
            byte[] pdfBytes = documentConversionService.readPdfFileAsBytes(pdfFile);

            // Xóa file tạm thời ngay sau khi đọc xong
            documentConversionService.deleteFileIfExists(tempFile);
            documentConversionService.deleteFileIfExists(pdfFile);

            // Trả file PDF về client dưới dạng stream mà không lưu trữ
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename*=UTF-8''" + URLEncoder.encode(pdfFile.getName(), StandardCharsets.UTF_8))
                    .body(pdfBytes);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File conversion failed");
        } finally {
            // Đảm bảo xóa file tạm thời dù có lỗi hay không
            documentConversionService.deleteFileIfExists(tempFile);
            documentConversionService.deleteFileIfExists(pdfFile);
        }
    }
}
