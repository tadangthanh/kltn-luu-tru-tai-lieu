package vn.kltn.controller.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.kltn.dto.request.PermissionRequest;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.dto.response.PermissionResponse;
import vn.kltn.dto.response.ResponseData;
import vn.kltn.service.impl.IFolderPermissionService;
import vn.kltn.validation.Create;
import vn.kltn.validation.Update;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/folders-permission")
public class FolderPermissionRest {
    private final IFolderPermissionService folderPermissionService;

    @PostMapping("/{folderId}")
    public ResponseData<PermissionResponse> addPermissionFolder(@PathVariable("folderId") Long folderId, @Validated(Create.class)
    @RequestBody PermissionRequest permissionRequest) {
        return new ResponseData<>(200, "Thành công", folderPermissionService.setPermissionResource(folderId, permissionRequest));
    }

    @PutMapping("/{permissionId}")
    public ResponseData<PermissionResponse> updatePermission(@PathVariable("permissionId") Long permissionId, @Validated(Update.class)
    @RequestBody PermissionRequest permissionRequest) {
        return new ResponseData<>(200, "Thành công", folderPermissionService.updatePermission(permissionId, permissionRequest));
    }

    @GetMapping("/{folderId}")
    public ResponseData<PageResponse<List<PermissionResponse>>> getPagePermissionByResource(@PathVariable Long folderId, Pageable pageable) {
        PageResponse<List<PermissionResponse>> pageResponse = folderPermissionService.getPagePermissionByResourceId(folderId, pageable);
        return new ResponseData<>(200, "Thành công", pageResponse);
    }

    @DeleteMapping("/{permissionId}")
    public ResponseData<Void> deletePermission(@PathVariable Long permissionId) {
        folderPermissionService.deletePermissionById(permissionId);
        return new ResponseData<>(200, "Thành công", null);
    }
}
