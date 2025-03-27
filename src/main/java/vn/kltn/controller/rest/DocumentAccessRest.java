package vn.kltn.controller.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.kltn.dto.request.AccessRequest;
import vn.kltn.dto.response.DocumentAccessResponse;
import vn.kltn.dto.response.ResponseData;
import vn.kltn.service.IAccessService;
import vn.kltn.validation.Create;
import vn.kltn.validation.Update;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/documents-access")
@Validated
public class DocumentAccessRest {
    private final IAccessService<DocumentAccessResponse> documentAccessService;

    @PostMapping("/document/{documentId}")
    public ResponseData<DocumentAccessResponse> copy(@PathVariable Long documentId, @Validated(Create.class) @RequestBody AccessRequest accessRequest) {
        return new ResponseData<>(201, "Thành công", documentAccessService.createAccess(documentId, accessRequest));
    }

    @DeleteMapping("/{accessId}")
    public ResponseData<Void> delete(@PathVariable Long accessId) {
        documentAccessService.deleteAccess(accessId);
        return new ResponseData<>(204, "Thành công");
    }

    @PutMapping("/{accessId}")
    public ResponseData<DocumentAccessResponse> update(@PathVariable Long accessId, @Validated(Update.class) @RequestBody AccessRequest accessRequest) {
        return new ResponseData<>(200, "Thành công", documentAccessService.updateAccess(accessId, accessRequest.getPermission()));
    }
}
