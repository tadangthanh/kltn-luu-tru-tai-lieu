package vn.kltn.controller.rest;

import jakarta.validation.Valid;
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

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/permissions")
@Validated
public class PermissionRest {
    private final IPermissionService itemPermissionServiceImpl;

    @GetMapping("/item/{itemId}")
    public ResponseData<PageResponse<List<ItemPermissionResponse>>> getPermissionsByItemId(@PathVariable Long itemId, Pageable pageable) {
        return new ResponseData<>(200, "Thành công", itemPermissionServiceImpl.getPagePermissionByItemId(itemId, pageable));
    }

    @PostMapping("/item/{itemId}")
    public ResponseData<ItemPermissionResponse> addPermission(@PathVariable Long itemId,@Validated(Create.class) @RequestBody PermissionRequest permissionRequest) {
        return new ResponseData<>(200, "Thành công", itemPermissionServiceImpl.addPermission(itemId, permissionRequest));
    }

}
