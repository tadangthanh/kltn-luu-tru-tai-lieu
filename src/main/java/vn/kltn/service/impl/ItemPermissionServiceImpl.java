package vn.kltn.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.kltn.common.ItemType;
import vn.kltn.dto.request.PermissionRequest;
import vn.kltn.dto.response.ItemPermissionResponse;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.entity.Item;
import vn.kltn.entity.Permission;
import vn.kltn.entity.User;
import vn.kltn.exception.InvalidDataException;
import vn.kltn.map.PermissionMapper;
import vn.kltn.repository.PermissionRepo;
import vn.kltn.repository.util.PaginationUtils;
import vn.kltn.service.*;

import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j(topic = "ITEM_PERMISSION_SERVICE")
public class ItemPermissionServiceImpl implements IPermissionService {
    private final PermissionRepo permissionRepo;
    private final PermissionMapper permissionMapper;
    private final IPermissionValidatorService permissionValidatorService;
    private final IItemService iItemService;
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

    @Override
    public List<ItemPermissionResponse> addOrUpdatePermission(Long itemId, List<PermissionRequest> permissionsRequest) {
        log.info("Add or update permission for itemId: {}", itemId);

        if (permissionsRequest == null || permissionsRequest.isEmpty()) {
            return Collections.emptyList();
        }

        Item item = iItemService.getItemByIdOrThrow(itemId);
        List<Permission> permissionsToSave = new ArrayList<>();

        for (PermissionRequest request : permissionsRequest) {
            if (Boolean.TRUE.equals(request.getIsDelete())) {
                if (request.getId() != null) {
                    permissionRepo.deleteById(request.getId());
                }
                continue;
            }

            User recipient = userService.getUserById(request.getRecipientId());
            Permission permission = permissionRepo
                    .findByRecipientAndItem(recipient.getId(), item.getId())
                    .orElseGet(Permission::new);

            permission.setItem(item);
            permission.setRecipient(recipient);
            permission.setPermission(request.getPermission());

            permissionsToSave.add(permission);
        }

        if (!permissionsToSave.isEmpty()) {
            permissionRepo.saveAll(permissionsToSave);
        }

        return permissionMapper.toListItemPermissionResponse(permissionsToSave);
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
        log.info("update permission for permissionId: {}, permissionRequest: {}", permissionId, permissionRequest);
        Permission permission = getPermissionByIdOrThrow(permissionId);
        permission.setPermission(permissionRequest.getPermission());
        permission = permissionRepo.save(permission);
        if ((permission.getItem().getItemType() == ItemType.FOLDER)) {
            // Nếu là folder, propagate quyền xuống tất cả con cháu
            permissionInheritanceService.updateAllChildNotCustom(permission);
        }
        return permissionMapper.toItemPermissionResponse(permission);
    }

    private Permission getPermissionByIdOrThrow(Long permissionId) {
        return permissionRepo.findById(permissionId).orElseThrow(() -> new InvalidDataException("Không tìm thấy quyền với id này"));
    }


    @Override
    public void deletePermissionById(Long permissionId) {
        permissionDeletionService.deleteByItemId(permissionId);
    }

    @Override
    public PageResponse<List<ItemPermissionResponse>> getPagePermissionByItemId(Long itemId, Pageable pageable) {
        log.info("get page permission by resource id: {}", itemId);
        Item item = iItemService.getItemByIdOrThrow(itemId);
        // kiểm tra xem user hiện tại có permission với resource hiện tại hay k ?
        User currentUser = authenticationService.getCurrentUser();
        permissionValidatorService.validatePermissionManager(item, currentUser);
        Page<Permission> pagePermission = permissionRepo.findAllByItemId(itemId, pageable);
        return PaginationUtils.convertToPageResponse(pagePermission, pageable, permissionMapper::toItemPermissionResponse);
    }


    @Override
    public void deleteByItemAndRecipientId(Long itemId, Long recipientId) {
        permissionDeletionService.deleteByItemAndRecipientId(itemId, recipientId);
    }

    @Override
    public void deletePermissionByItems(List<Long> itemIds) {
        permissionDeletionService.deletePermissionByItems(itemIds);
    }

    @Override
    public Set<Long> getItemIdsByRecipientId(Long recipientId) {
        return permissionRepo.findItemIdsByRecipientId(recipientId);
    }

    @Override
    public void deletePermissionByItemId(Long itemId) {
        permissionDeletionService.deleteByItemId(itemId);
    }
}
