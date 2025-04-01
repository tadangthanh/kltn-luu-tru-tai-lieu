package vn.kltn.controller.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.kltn.dto.request.PermissionRequest;
import vn.kltn.dto.response.PermissionResponse;
import vn.kltn.dto.response.ResponseData;
import vn.kltn.service.IDocumentPermissionService;
import vn.kltn.validation.Create;
import vn.kltn.validation.Update;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/documents-permission")
public class DocumentPermissionRest {
    private final IDocumentPermissionService documentPermissionService;

    @PostMapping("/{documentId}")
    public ResponseData<PermissionResponse> addPermissionDocument(@PathVariable("documentId") Long documentId,
                                                                  @Validated(Create.class) @RequestBody PermissionRequest permissionRequest) {
        return new ResponseData<>(200, "Thành công", documentPermissionService.setPermissionResource(documentId, permissionRequest));
    }

    @PutMapping("/{permissionId}")
    public ResponseData<PermissionResponse> updatePermission(@PathVariable("permissionId") Long permissionId,
                                                             @Validated(Update.class) @RequestBody PermissionRequest permissionRequest) {
        return new ResponseData<>(200, "Thành công", documentPermissionService.updatePermission(permissionId, permissionRequest));
    }
}
