package vn.kltn.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
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
import vn.kltn.util.PaginationUtils;
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
    private final ItemGetterService itemGetterService;
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

        Item item = itemGetterService.getItemByIdOrThrow(itemId);
        List<Permission> permissionsToSave = new ArrayList<>();

        for (PermissionRequest request : permissionsRequest) {
            // Xử lý xóa quyền nếu yêu cầu
            if (Boolean.TRUE.equals(request.getIsDelete())) {
                if (request.getId() != null) {
                    permissionDeletionService.deleteByPermissionId(request.getId());
                }
                continue; // tiếp tục với quyền kế tiếp
            }

            User recipient = userService.getUserById(request.getRecipientId());

            // Tìm Permission hiện có
            Optional<Permission> existingPermissionOpt = permissionRepo
                    .findByRecipientAndItem(recipient.getId(), item.getId());

            Permission permission;

            if (existingPermissionOpt.isPresent()) {
                // Nếu đã có Permission cho recipient và item, tiến hành cập nhật
                permission = existingPermissionOpt.get();
                log.info("Updating permission for recipient {} on item {}", recipient.getId(), item.getId());
                permission.setPermission(request.getPermission());
                // Nếu là folder, kế thừa quyền cho tất cả các item con (child items)
                if (permission.getItem().getItemType() == ItemType.FOLDER) {
                    permissionInheritanceService.updateAllChildNotCustom(permission);
                }

            } else {
                // Nếu không có Permission, tạo mới
                permission = new Permission();
                log.info("Creating new permission for recipient {} on item {}", recipient.getId(), item.getId());
                permission.setItem(item);
                permission.setRecipient(recipient);
                permission.setPermission(request.getPermission());
                if (item.getItemType() == ItemType.FOLDER) {
                    // Nếu là folder, propagate quyền xuống tất cả con cháu
                    permissionInheritanceService.propagatePermissions(item.getId(), permission);
                }
            }

            // Thêm vào danh sách để save
            permissionsToSave.add(permission);


        }

        // Save tất cả các Permission (cả create mới và update)
        if (!permissionsToSave.isEmpty()) {
            permissionRepo.saveAll(permissionsToSave);
        }

        // Chuyển đổi thành các response để trả về
        return permissionMapper.toListItemPermissionResponse(permissionsToSave);
    }


    private void handlePermissionUpdateAfterSave(Item item, Permission permission) {
        if (item.getItemType() == ItemType.FOLDER) {
            // Nếu là folder, propagate quyền xuống tất cả con cháu
            permissionInheritanceService.propagatePermissions(item.getId(), permission);
        }
    }

    private Permission createPermissionFromRequest(PermissionRequest permissionRequest, Long itemId) {
        Item item = itemGetterService.getItemByIdOrThrow(itemId);
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
        Item item = itemGetterService.getItemByIdOrThrow(itemId);
        // kiểm tra xem user hiện tại có permission với resource hiện tại hay k ?
        User currentUser = authenticationService.getCurrentUser();
        permissionValidatorService.validatePermissionEditor(item, currentUser);
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

    @Override
    public Permission getPermissionItemByRecipientId(Long itemId, Long recipientId) {
        return permissionRepo.findByItemIdAndRecipientId(itemId, recipientId).orElseThrow(() -> {
            log.warn("Permission not found for itemId: {}, recipientId: {}", itemId, recipientId);
            return new AccessDeniedException("Bạn không có quyền truy cập tài liệu này");
        });
    }

    @Override
    public Boolean hasPermissionEditorOrOwner(Long itemId) {
        User currentUser = authenticationService.getCurrentUser();
        Item item = itemGetterService.getItemByIdOrThrow(itemId);
        if (item.getOwner().getId().equals(currentUser.getId())) {
            return true;
        }
        Permission permission = permissionRepo.findByItemIdAndRecipientId(itemId, currentUser.getId()).orElse(null);
        if (permission != null) {
            return permission.getPermission().name().equalsIgnoreCase("editor") && permission.isPermissionManager();
        } else {
            return false;
        }
    }

    @Override
    public void hidePermissionByItemIdAndUserId(Long itemId, Long userId) {
        log.info("hide permission by itemId: {}, userId: {}", itemId, userId);
        Permission permission = permissionRepo.findByItemIdAndRecipientId(itemId, userId).orElseThrow(() -> {
            log.warn("Permission not found for itemId: {}, recipientId: {}", itemId, userId);
            return new AccessDeniedException("Bạn không có quyền truy cập tài liệu này");
        });
        permission.setHidden(true);
        permissionRepo.save(permission);
    }

    @Override
    public void showItem(Long itemId) {
        log.info("show item id: {}",itemId);
        User currentUser= authenticationService.getCurrentUser();
        permissionRepo.showItem(itemId,currentUser.getId());
    }
}
