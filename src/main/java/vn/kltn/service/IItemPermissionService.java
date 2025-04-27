package vn.kltn.service;

import org.springframework.data.domain.Pageable;
import vn.kltn.dto.request.PermissionRequest;
import vn.kltn.dto.response.ItemPermissionResponse;
import vn.kltn.dto.response.PageResponse;

import java.util.List;

public interface IItemPermissionService {
    PageResponse<List<ItemPermissionResponse>> getPermissionsByItemId(Long itemId, Pageable pageable);

    ItemPermissionResponse addPermission(Long itemId, PermissionRequest permissionRequest);
}
