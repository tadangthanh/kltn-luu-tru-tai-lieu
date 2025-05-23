package vn.kltn.controller.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.kltn.dto.response.ResponseData;
import vn.kltn.service.IDocumentConversionService;
import vn.kltn.validation.ValidFile;

import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/v1/document-conversion")
@RequiredArgsConstructor
@Slf4j(topic = "DOCUMENT_CONVERSION_REST")
@Validated
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

    @PostMapping("/convert")
    public ResponseData<String> convert(
            @ValidFile @RequestParam("file") MultipartFile file, @RequestParam("format") String format) {
        return new ResponseData<>(200, "thành công", documentConversionService.convertFile(file, format));
    }

    @GetMapping("/convert-blob")
    public ResponseData<String> convertBlob(@RequestParam("blobName") String blobName, @RequestParam("format") String format) {
        return new ResponseData<>(200, "thành công", documentConversionService.convertStoredFile(blobName, format));
    }

}
