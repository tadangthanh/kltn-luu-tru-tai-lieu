package vn.kltn.controller.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.kltn.dto.response.DocumentVersionResponse;
import vn.kltn.dto.response.ResponseData;
import vn.kltn.service.IDocumentVersionService;

@RequiredArgsConstructor
@RequestMapping("/api/v1/document-versions")
@RestController
public class DocumentVersionRest {
    private final IDocumentVersionService documentVersionService;

    @PostMapping("/{documentId}/versions/{targetVersionId}/restore")
    public ResponseData<DocumentVersionResponse> restoreVersion(@PathVariable Long targetVersionId, @PathVariable Long documentId) {
        return new ResponseData<>(200, "thành công", documentVersionService.restoreVersion(documentId, targetVersionId));
    }

}
