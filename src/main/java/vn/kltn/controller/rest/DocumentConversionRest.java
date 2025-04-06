package vn.kltn.controller.rest;

import lombok.RequiredArgsConstructor;
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
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/v1/document-conversion")
@RequiredArgsConstructor
public class DocumentConversionRest {
    private final IDocumentConversionService documentConversionService;
    // Đường dẫn nơi lưu trữ file tạm thời trên server
    private static final String TEMP_DIR = "D:\\export\\temp\\";

//    // Endpoint để upload tài liệu Word và chuyển đổi thành PDF
//    @PostMapping("/word-to-pdf")
//    public ResponseEntity<byte[]> convertUploadedDocument(@RequestParam("file") MultipartFile file) throws Exception {
//        if (file.isEmpty()) {
//            return ResponseEntity.badRequest().body("Vui lòng upload một tệp Word!".getBytes());
//        }
//
//        InputStream inputStream = file.getInputStream();
//        byte[] pdfBytes = documentConversionService.convertWordToPdf(inputStream);
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_PDF);
//        headers.setContentDispositionFormData("attachment", "converted.pdf");
//
//        return ResponseEntity.ok().headers(headers).body(pdfBytes);
//    }

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
            e.printStackTrace();
            return "Lỗi xử lý: " + e.getMessage();
        }
    }

    @PostMapping("/word-to-pdf")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No file uploaded");
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
