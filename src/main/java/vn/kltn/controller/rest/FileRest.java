package vn.kltn.controller.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.kltn.dto.request.FileRequest;
import vn.kltn.dto.response.FileDownloadResponse;
import vn.kltn.dto.response.FileResponse;
import vn.kltn.dto.response.ResponseData;
import vn.kltn.service.IFileService;

@RestController
@RequestMapping("/api/v1/file")
@RequiredArgsConstructor
@Validated
public class FileRest {
    private final IFileService fileService;

    @PostMapping("/repo/{repoId}/upload")
    public ResponseData<FileResponse> upload(@RequestPart("file") MultipartFile file, @Valid
    @RequestPart("data") FileRequest request, @PathVariable Long repoId) {
        return new ResponseData<>(201, "Upload file successfully", fileService.uploadFile(repoId, request, file));
    }

    @DeleteMapping("/{fileId}")
    public ResponseData<Void> delete(@PathVariable Long fileId) {
        fileService.deleteFile(fileId);
        return new ResponseData<>(204, "Delete file successfully", null);
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<byte[]> download(@PathVariable Long fileId) {
        FileDownloadResponse fileDownloadResponse = fileService.downloadFile(fileId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(fileDownloadResponse.getFileType())); // Kiểu file chung
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(fileDownloadResponse.getFileName())
                .build());
        return ResponseEntity.ok()
                .headers(headers)
                .body(fileDownloadResponse.getData());
    }


    @PutMapping("/{fileId}")
    public ResponseData<FileResponse> update(@Valid @RequestBody FileRequest request, @PathVariable Long fileId) {
        return new ResponseData<>(200, "Update file successfully", fileService.updateFileMetadata(fileId, request));
    }


}
