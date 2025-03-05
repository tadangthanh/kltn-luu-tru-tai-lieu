package vn.kltn.controller.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.kltn.dto.request.FileShareRequest;
import vn.kltn.dto.response.FileDataResponse;
import vn.kltn.dto.response.FileShareResponse;
import vn.kltn.dto.response.ResponseData;
import vn.kltn.service.IFileShareService;

import java.io.ByteArrayInputStream;

@RestController
@RequestMapping("/api/v1/file-share")
@Validated
@RequiredArgsConstructor
public class FileShareRest {
    private final IFileShareService fileShareService;

    @GetMapping("/{token}")
    public ResponseEntity<InputStreamResource> viewFile(@PathVariable("token") String token, @RequestParam(value = "password", required = false) String password) {
        FileDataResponse fileDataResponse = fileShareService.viewFile(token, password);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileDataResponse.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(fileDataResponse.getFileType()))
                .body(new InputStreamResource(new ByteArrayInputStream(fileDataResponse.getData())));
    }

    @PostMapping("/{fileId}/share")
    public ResponseData<FileShareResponse> share(@PathVariable Long fileId, @Valid @RequestBody FileShareRequest fileShareRequest) {
        return new ResponseData<>(201, "Share file successfully", fileShareService.createFileShareLink(fileId, fileShareRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseData<Void> delete(@PathVariable Long id) {
        fileShareService.deleteFileShareById(id);
        return new ResponseData<>(200, "Delete file share successfully", null);
    }
}
