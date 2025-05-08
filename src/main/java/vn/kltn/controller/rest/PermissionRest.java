package vn.kltn.controller.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.kltn.dto.request.PermissionRequest;
import vn.kltn.dto.response.ItemPermissionResponse;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.dto.response.ResponseData;
import vn.kltn.service.IPermissionService;
import vn.kltn.validation.Create;
import vn.kltn.validation.Update;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/permissions")
@Validated
public class PermissionRest {
    private final IPermissionService permissionService;

    @GetMapping("/item/{itemId}")
    public ResponseData<PageResponse<List<ItemPermissionResponse>>> getPermissionsByItemId(@PathVariable Long itemId, Pageable pageable) {
        return new ResponseData<>(200, "Thành công", permissionService.getPagePermissionByItemId(itemId, pageable));
    }

    @PostMapping("/item/{itemId}")
    public ResponseData<ItemPermissionResponse> addPermission(@PathVariable Long itemId, @Validated(Create.class) @RequestBody PermissionRequest permissionRequest) {
        return new ResponseData<>(201, "Thành công", permissionService.addPermission(itemId, permissionRequest));
    }

    @PostMapping("/item/{itemId}/batch")
    public ResponseData<List<ItemPermissionResponse>> addOrUpdateList(@PathVariable Long itemId, @Validated(Create.class) @RequestBody List<PermissionRequest> permissionsRequest) {
        return new ResponseData<>(201, "Thành công", permissionService.addOrUpdatePermission(itemId, permissionsRequest));
    }

    @PutMapping("/{permissionId}")
    public ResponseData<ItemPermissionResponse> updatePermission(@PathVariable Long permissionId, @Validated(Update.class) @RequestBody PermissionRequest permissionRequest) {
        return new ResponseData<>(200, "Thành công", permissionService.updatePermission(permissionId, permissionRequest));
    }

    @DeleteMapping("/{permissionId}")
    public ResponseData<Void> deletePermission(@PathVariable Long permissionId) {
        permissionService.deletePermissionById(permissionId);
        return new ResponseData<>(200, "Thành công");
    }

    @GetMapping("/has-permission/{itemId}")
    public ResponseData<Boolean> hasPermissionEditorOrOwner(@PathVariable Long itemId) {
        return new ResponseData<>(200, "Thành công", permissionService.hasPermissionEditorOrOwner(itemId));
    }
}
