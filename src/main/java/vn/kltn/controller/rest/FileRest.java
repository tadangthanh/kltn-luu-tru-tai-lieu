package vn.kltn.controller.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.kltn.dto.request.FileRequest;
import vn.kltn.dto.response.FileDataResponse;
import vn.kltn.dto.response.FileResponse;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.dto.response.ResponseData;
import vn.kltn.service.IFileService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/file")
@RequiredArgsConstructor
@Validated
public class FileRest {
    private final IFileService fileService;

    @PostMapping("/repo/{repoId}/upload")
    public ResponseData<FileResponse> upload(@RequestPart("file") MultipartFile file, @Valid @RequestPart("data") FileRequest request, @PathVariable Long repoId) {
        return new ResponseData<>(201, "Upload file successfully", fileService.uploadFile(repoId, request, file));
    }

    @DeleteMapping("/{fileId}")
    public ResponseData<Void> delete(@PathVariable Long fileId) {
        fileService.deleteFile(fileId);
        return new ResponseData<>(204, "Delete file successfully", null);
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<byte[]> download(@PathVariable Long fileId) {
        FileDataResponse fileDataResponse = fileService.downloadFile(fileId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(fileDataResponse.getFileType())); // Kiểu file chung
        headers.setContentDisposition(ContentDisposition.attachment().filename(fileDataResponse.getFileName()).build());
        return ResponseEntity.ok().headers(headers).body(fileDataResponse.getData());
    }


    @PutMapping("/{fileId}")
    public ResponseData<FileResponse> update(@Valid @RequestBody FileRequest request, @PathVariable Long fileId) {
        return new ResponseData<>(200, "Update file successfully", fileService.updateFileMetadata(fileId, request));
    }

    @GetMapping
    public ResponseData<PageResponse<List<FileResponse>>> searchFile(Pageable pageable, @RequestParam(required = false, value = "file") String[] file) {
        return new ResponseData<>(200, "Search file successfully", fileService.advanceSearchBySpecification(pageable, file));
    }

    @PatchMapping("/{fileId}/restore")
    public ResponseData<FileResponse> restore(@PathVariable Long fileId) {
        return new ResponseData<>(201, "Restore file successfully", fileService.restoreFile(fileId));
    }

    @GetMapping("/tag")
    public ResponseData<PageResponse<List<FileResponse>>> searchByTagName(Pageable pageable, @RequestParam String tagName) {
        return new ResponseData<>(200, "Search file by tag name successfully", fileService.searchByTagName(pageable, tagName));
    }
}
