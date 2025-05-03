package vn.kltn.controller.rest;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import vn.kltn.dto.response.DocumentVersionResponse;
import vn.kltn.dto.response.ResponseData;
import vn.kltn.entity.Document;
import vn.kltn.entity.DocumentVersion;
import vn.kltn.service.IDocumentVersionService;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static vn.kltn.repository.util.FileUtil.*;

@RequiredArgsConstructor
@RequestMapping("/api/v1/document-versions")
@RestController
public class DocumentVersionRest {
    private final IDocumentVersionService documentVersionService;

    @PostMapping("/{documentId}/versions/{targetVersionId}/restore")
    public ResponseData<DocumentVersionResponse> restoreVersion(@PathVariable Long targetVersionId, @PathVariable Long documentId) {
        return new ResponseData<>(200, "thành công", documentVersionService.restoreVersion(documentId, targetVersionId));
    }

    @GetMapping("/{documentId}/versions")
    public ResponseData<List<DocumentVersionResponse>> getVersionsByDocumentId(@PathVariable Long documentId) {
        return new ResponseData<>(200, "thành công", documentVersionService.getVersionsByDocumentId(documentId));
    }

    @GetMapping("/{versionId}/download")
    public void downloadDoc(@PathVariable Long versionId, HttpServletResponse response) throws IOException {
        DocumentVersion documentVersion = documentVersionService.getVersionByIdOrThrow(versionId);
        try (InputStream inputStream = documentVersionService.downloadVersion(versionId)) {
            Document document = documentVersion.getDocument();
            String fileName = document.getName().split("\\.")[0] + "_version_" + documentVersion.getVersion() +"." + documentVersion.getBlobName().split("\\.")[1];
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                response.getOutputStream().write(buffer, 0, bytesRead);
            }
            response.flushBuffer();
        }
    }

}
