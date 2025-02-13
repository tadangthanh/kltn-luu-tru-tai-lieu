package vn.kltn.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.kltn.service.IFileStorageService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping("/api/blob")
@RequiredArgsConstructor
public class FileController {
    private final IFileStorageService azureFileStorageService;

//    @PostMapping("/{upload}")
//    public ResponseEntity<InputStreamResource> upload(@RequestParam MultipartFile file) throws IOException {
//        String blobName = this.azureFileStorageService.upload(file);
//        return ResponseEntity.ok()
//                .contentType(MediaType.APPLICATION_OCTET_STREAM)
//                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getOriginalFilename() + "\"")
//                .body(new InputStreamResource(new ByteArrayInputStream(blobName.getBytes())));
//    }

    @PostMapping("/{upload}")
    public ResponseEntity<String> upload(@RequestParam MultipartFile file) throws IOException {
       try(InputStream inputStream = file.getInputStream()) {
           String blobName = this.azureFileStorageService.uploadChunked(inputStream, file.getOriginalFilename(), file.getSize(), 10 * 1024 * 1024);
              return ResponseEntity.ok(blobName);
       }
    }
}
