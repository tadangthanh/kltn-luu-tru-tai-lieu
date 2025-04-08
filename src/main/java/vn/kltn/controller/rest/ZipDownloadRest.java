package vn.kltn.controller.rest;


import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.kltn.service.IAzureStorageService;
import vn.kltn.service.impl.ZipEntryDescriptor;
import vn.kltn.service.impl.ZipService;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/api/v1/docs")
@RequiredArgsConstructor
public class ZipDownloadRest {

    private final IAzureStorageService azureStorageService;
    private final ZipService zipService;

    @GetMapping("/download-zip")
    public void downloadZip(@RequestParam List<String> blobNames, HttpServletResponse response) throws IOException {
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=\"tai-lieu.zip\"");

        List<ZipEntryDescriptor> entries = blobNames.stream()
                .map(blob -> new ZipEntryDescriptor(
                        generateFileName(blob), // Tên file trong zip
                        CompletableFuture.supplyAsync(() -> azureStorageService.downloadBlobInputStream(blob)) // Stream tải file
                )).toList();

        zipService.streamZipToOutput(entries, response.getOutputStream());
    }

    private String generateFileName(String blobName) {
        // Ví dụ: abc_xyz123.pdf -> Tai lieu - xyz123.pdf
        String baseName = blobName.substring(blobName.lastIndexOf("/") + 1);
        return "Tai lieu - " + baseName;
    }
}
