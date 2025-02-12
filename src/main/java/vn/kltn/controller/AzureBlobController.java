package vn.kltn.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.kltn.service.IAzureService;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@RestController
@RequestMapping("/api/blob")
@RequiredArgsConstructor
public class AzureBlobController {
    private final IAzureService azureService;

    @PostMapping("/{upload}")
    public ResponseEntity<InputStreamResource> upload(@RequestParam MultipartFile file) throws IOException {
        String blobName = this.azureService.upload(file);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getOriginalFilename() + "\"")
                .body(new InputStreamResource(new ByteArrayInputStream(blobName.getBytes())));
    }

}
