package vn.kltn.controller.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.kltn.dto.request.FileRequest;
import vn.kltn.dto.request.FileShareRequest;
import vn.kltn.dto.response.*;
import vn.kltn.service.IFileService;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
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

    @GetMapping("/repo/{repoId}")
    public ResponseData<PageResponse<List<FileResponse>>> searchFile(Pageable pageable, @PathVariable Long repoId, @RequestParam(required = false, value = "file") String[] file) {
        return new ResponseData<>(200, "Search file successfully", fileService.advanceSearchBySpecification(repoId, pageable, file));
    }

    @PatchMapping("/{fileId}/restore")
    public ResponseData<FileResponse> restore(@PathVariable Long fileId) {
        return new ResponseData<>(201, "Restore file successfully", fileService.restoreFile(fileId));
    }

    @GetMapping("/repo/{repoId}/tag")
    public ResponseData<PageResponse<List<FileResponse>>> searchByTagName(Pageable pageable, @PathVariable Long repoId, @RequestParam String tagName) {
        return new ResponseData<>(200, "Search file by tag name successfully", fileService.searchByTagName(repoId, tagName, pageable));
    }

    @GetMapping("/repo/{repoId}/search-by-date-range")
    public ResponseData<PageResponse<List<FileResponse>>> searchByStartDateAndEndDate(Pageable pageable, @PathVariable Long repoId,
                                                                                      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                                                      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return new ResponseData<>(200, "Search file by date range successfully", fileService.searchByStartDateAndEndDate(repoId, pageable, startDate, endDate));
    }

    @GetMapping("/view/{token}")
    public ResponseEntity<InputStreamResource> viewFile(@PathVariable("token") String token, @RequestParam(value = "password", required = false) String password) {
        FileDataResponse fileDataResponse = fileService.viewFile(token, password);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileDataResponse.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(fileDataResponse.getFileType()))
                .body(new InputStreamResource(new ByteArrayInputStream(fileDataResponse.getData())));
    }

    @PostMapping("/{fileId}/share")
    public ResponseData<FileShareResponse> share(@PathVariable Long fileId, @Valid @RequestBody FileShareRequest fileShareRequest) {
        return new ResponseData<>(201, "Share file successfully", fileService.shareFile(fileId, fileShareRequest));
    }

    @DeleteMapping("/file-share/{fileId}")
    public ResponseData<Void> unFileShare(@PathVariable Long fileId) {
        fileService.deleteFileShareByFileId(fileId);
        return new ResponseData<>(200, "un file share completed", null);
    }
}
