package vn.kltn.controller.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.kltn.dto.response.FileShareView;
import vn.kltn.service.IFileShareService;

import java.io.ByteArrayInputStream;

@RestController
@RequestMapping("/api/v1/file-share")
@RequiredArgsConstructor
public class FileShareRest {
    private final IFileShareService fileShareService;

    @GetMapping("/{token}")
    public ResponseEntity<InputStreamResource> getAudio(@PathVariable("token") String token, @RequestParam("password") String password) {
        FileShareView fileShareView = fileShareService.viewFile(token, password);
        String contentType = fileShareView.getFileType();
        byte[] data = fileShareView.getFileBytes();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileShareView.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(contentType)) // Định dạng file âm thanh
                .body(new InputStreamResource(new ByteArrayInputStream(data)));
    }
}
