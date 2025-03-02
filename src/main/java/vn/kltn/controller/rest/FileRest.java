package vn.kltn.controller.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.kltn.dto.request.FileRequest;
import vn.kltn.dto.response.FileResponse;
import vn.kltn.dto.response.ResponseData;
import vn.kltn.service.IFileService;

@RestController
@RequestMapping("/api/v1/file")
@RequiredArgsConstructor
public class FileRest {
    private final IFileService fileService;

    @PostMapping("/repo/{repoId}/upload")
    public ResponseData<FileResponse> upload(@RequestPart("file") MultipartFile file,
                                             @RequestPart("data") FileRequest request, @PathVariable Long repoId) {
        return new ResponseData<>(201, "Upload file successfully", fileService.uploadFile(repoId, request, file));
    }

    @DeleteMapping("/{fileId}")
    public ResponseData<Void> delete(@PathVariable Long fileId) {
        fileService.deleteFile(fileId);
        return new ResponseData<>(204, "Delete file successfully", null);
    }


}
