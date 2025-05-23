package vn.kltn.service;

import org.springframework.data.domain.Pageable;
import vn.kltn.dto.request.PermissionRequest;
import vn.kltn.dto.response.ItemPermissionResponse;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.entity.Permission;

import java.util.List;
import java.util.Set;

public interface IPermissionService {
    ItemPermissionResponse addPermission(Long itemId, PermissionRequest permissionRequest);

    List<ItemPermissionResponse> addOrUpdatePermission(Long itemId, List<PermissionRequest> permissionsRequest);

    ItemPermissionResponse updatePermission(Long permissionId, PermissionRequest permissionRequest);

    void deletePermissionById(Long permissionId);

    PageResponse<List<ItemPermissionResponse>> getPagePermissionByItemId(Long itemId, Pageable pageable);

    void deleteByItemAndRecipientId(Long resourceId, Long recipientId);

    void deletePermissionByItems(List<Long> resourceIds);

    Set<Long> getItemIdsByRecipientId(Long recipientId);

    void deletePermissionByItemId(Long resourceId);

    Permission getPermissionItemByRecipientId(Long itemId, Long recipientId);

    Boolean hasPermissionEditorOrOwner(Long itemId);

    void hidePermissionByItemIdAndUserId(Long itemId, Long userId);

    void showItem(Long itemId);
}
