package vn.kltn.controller.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.kltn.service.IAzureStorageService;

import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping("/api/blob")
@RequiredArgsConstructor
public class FileRest {
    private final IAzureStorageService azureFileStorageService;

    @PostMapping("/{upload}")
    public ResponseEntity<String> upload(@RequestParam MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream()) {
            String blobName = this.azureFileStorageService.uploadChunked(inputStream, file.getOriginalFilename(), file.getSize(), 10 * 1024 * 1024);
            return ResponseEntity.ok(blobName);
        }
    }

    @GetMapping("/list")
    @PreAuthorize("hasAnyAuthority('manager', 'admin')")
    public String getList() {
        return "get List";
    }

    @GetMapping("/detail")
    @PreAuthorize("hasAnyAuthority('user')")
    public String getUserDetail() {
        return "get User Detail";
    }
}
