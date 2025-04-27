package vn.kltn.service;

import org.springframework.data.domain.Pageable;
import vn.kltn.dto.request.PermissionRequest;
import vn.kltn.dto.response.ItemPermissionResponse;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.entity.Item;

import java.util.List;

public interface IPermissionService {
     ItemPermissionResponse addPermission(Long itemId, PermissionRequest permissionRequest);

    ItemPermissionResponse updatePermission(Long permissionId, PermissionRequest permissionRequest);

    void inheritPermissions(Item item);

    void deletePermissionById(Long permissionId);

    PageResponse<List<ItemPermissionResponse>> getPagePermissionByItemId(Long itemId, Pageable pageable);

    void validateUserIsEditor(Long resourceId, Long userId);

    void deleteByResourceAndRecipientId(Long resourceId, Long recipientId);

    void deletePermissionByResourceIds(List<Long> resourceIds);

    void deletePermissionByResourceId(Long resourceId);

}
