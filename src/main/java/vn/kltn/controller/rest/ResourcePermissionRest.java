package vn.kltn.controller.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.kltn.dto.request.PermissionRequest;
import vn.kltn.dto.response.PermissionResponse;
import vn.kltn.dto.response.ResponseData;
import vn.kltn.service.impl.IFolderPermissionService;
import vn.kltn.validation.Create;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/folders-permission")
public class ResourcePermissionRest {
    private final IFolderPermissionService folderPermissionService;

    @PostMapping("/{folderId}")
    public ResponseData<PermissionResponse> addPermissionFolder(@PathVariable("folderId") Long folderId, @Validated(Create.class)
    @RequestBody PermissionRequest permissionRequest) {
        return new ResponseData<>(200, "Thành công", folderPermissionService.setPermissionResource(folderId, permissionRequest));
    }
}
