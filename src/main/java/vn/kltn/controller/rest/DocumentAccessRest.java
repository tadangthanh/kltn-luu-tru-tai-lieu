package vn.kltn.controller.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.kltn.dto.request.AccessRequest;
import vn.kltn.dto.response.AccessResourceResponse;
import vn.kltn.dto.response.DocumentResponse;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.dto.response.ResponseData;
import vn.kltn.service.IDocumentAccessService;
import vn.kltn.validation.Create;
import vn.kltn.validation.Update;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/documents-access")
@Validated
public class DocumentAccessRest {
    private final IDocumentAccessService documentAccessService;

    @PostMapping("/document/{documentId}")
    public ResponseData<AccessResourceResponse> copy(@PathVariable Long documentId, @Validated(Create.class) @RequestBody AccessRequest accessRequest) {
        return new ResponseData<>(201, "Thành công", documentAccessService.createAccess(documentId, accessRequest));
    }

    @DeleteMapping("/{accessId}")
    public ResponseData<Void> delete(@PathVariable Long accessId) {
        documentAccessService.deleteAccess(accessId);
        return new ResponseData<>(204, "Thành công");
    }

    @PutMapping("/{accessId}")
    public ResponseData<AccessResourceResponse> update(@PathVariable Long accessId, @Validated(Update.class) @RequestBody AccessRequest accessRequest) {
        return new ResponseData<>(200, "Thành công", documentAccessService.updateAccess(accessId, accessRequest.getPermission()));
    }

    @GetMapping
    public ResponseData<PageResponse<List<AccessResourceResponse>>> getAccessByResource(Pageable pageable, @RequestParam(required = false) String[] resources) {
        return new ResponseData<>(200, "Thành công", documentAccessService.getAccessByResource(pageable, resources));
    }

    @GetMapping("/document")
    public ResponseData<PageResponse<List<DocumentResponse>>> getDocumentSharedByCurrentUser(Pageable pageable) {
        return new ResponseData<>(200, "Thành công", documentAccessService.getPageDocumentSharedByCurrentUser(pageable));
    }
}
