package vn.kltn.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.kltn.common.ItemType;
import vn.kltn.dto.request.PermissionRequest;
import vn.kltn.dto.response.ItemPermissionResponse;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.entity.Item;
import vn.kltn.entity.Permission;
import vn.kltn.map.PermissionMapper;
import vn.kltn.repository.PermissionRepo;
import vn.kltn.service.*;
import vn.kltn.util.ItemValidator;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j(topic = "ITEM_PERMISSION_SERVICE")
public class ItemPermissionServiceImpl implements IPermissionService {
    private final PermissionRepo permissionRepo;
    private final PermissionMapper permissionMapper;
    private final IPermissionValidatorService permissionValidatorService;
    private final IItemService iItemService;
    private final ItemValidator itemValidator;
    private final IPermissionDeletionService permissionDeletionService;
    private final IAuthenticationService authenticationService;
    private final IPermissionInheritanceService permissionInheritanceService;
    private final IUserService userService;

    @Override
    public ItemPermissionResponse addPermission(Long itemId, PermissionRequest permissionRequest) {
        log.info("Setting permission for itemId: {}, permission: {}, recipientId: {}", itemId, permissionRequest.getPermission(), permissionRequest.getRecipientId());
        // validator
        permissionValidatorService.validateAddPermissionRequest(itemId, permissionRequest);
        // create permission
        Permission permission = createPermissionFromRequest(permissionRequest, itemId);
        Permission savedPermission = permissionRepo.save(permission);
        handlePermissionUpdateAfterSave(permission.getItem(), savedPermission);
        return permissionMapper.toItemPermissionResponse(savedPermission);
    }

    private void handlePermissionUpdateAfterSave(Item item, Permission permission) {
        if (item.getItemType() == ItemType.FOLDER) {
            // Nếu là folder, propagate quyền xuống tất cả con cháu
            permissionInheritanceService.propagatePermissions(item.getId(), permission);
        }
    }

    private Permission createPermissionFromRequest(PermissionRequest permissionRequest, Long itemId) {
        Item item = iItemService.getItemByIdOrThrow(itemId);
        Permission permission = new Permission();
        permission.setPermission(permissionRequest.getPermission());
        permission.setRecipient(userService.getUserById(permissionRequest.getRecipientId()));
        permission.setItem(item);
        return permission;
    }

    @Override
    public ItemPermissionResponse updatePermission(Long permissionId, PermissionRequest permissionRequest) {
        return null;
    }

    @Override
    public void inheritPermissions(Item item) {

    }

    @Override
    public void deletePermissionById(Long permissionId) {

    }

    @Override
    public PageResponse<List<ItemPermissionResponse>> getPagePermissionByItemId(Long itemId, Pageable pageable) {
        return null;
    }

    @Override
    public void validateUserIsEditor(Long resourceId, Long userId) {

    }

    @Override
    public void deleteByResourceAndRecipientId(Long resourceId, Long recipientId) {

    }

    @Override
    public void deletePermissionByResourceIds(List<Long> resourceIds) {

    }

    @Override
    public void deletePermissionByResourceId(Long resourceId) {

    }
}
