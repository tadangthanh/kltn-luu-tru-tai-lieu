package vn.kltn.controller.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.kltn.dto.request.DocumentAccessRequest;
import vn.kltn.dto.response.DocumentAccessResponse;
import vn.kltn.dto.response.ResponseData;
import vn.kltn.service.IDocumentAccessService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/documents-access")
@Validated
public class DocumentAccessRest {
    private final IDocumentAccessService documentAccessService;

    @PostMapping("/document/{documentId}")
    public ResponseData<DocumentAccessResponse> copy(@PathVariable Long documentId, @Valid @RequestBody DocumentAccessRequest accessRequest) {
        return new ResponseData<>(201, "Thành công", documentAccessService.createDocumentAccess(documentId, accessRequest));
    }
}
