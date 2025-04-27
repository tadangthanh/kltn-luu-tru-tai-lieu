//package vn.kltn.service.impl;
//
//import jakarta.transaction.Transactional;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.security.access.AccessDeniedException;
//import org.springframework.stereotype.Service;
//import vn.kltn.dto.request.PermissionRequest;
//import vn.kltn.dto.response.ItemPermissionResponse;
//import vn.kltn.dto.response.PageResponse;
//import vn.kltn.entity.Item;
//import vn.kltn.entity.Permission;
//import vn.kltn.entity.User;
//import vn.kltn.exception.InvalidDataException;
//import vn.kltn.map.PermissionMapper;
//import vn.kltn.repository.PermissionRepo;
//import vn.kltn.repository.util.PaginationUtils;
//import vn.kltn.service.IAuthenticationService;
//import vn.kltn.service.IPermissionService;
//import vn.kltn.service.IUserService;
//import vn.kltn.util.ItemValidator;
//
//import java.util.List;
//
//@Service
//@Transactional
//@Slf4j
//public abstract class AbstractPermissionService implements IPermissionService {
//    protected final PermissionRepo permissionRepo;
//    protected final IUserService userService;
//    protected final PermissionMapper permissionMapper;
//    protected final ItemValidator itemValidator;
//    protected final IAuthenticationService authenticationService;
//
//    protected AbstractPermissionService(PermissionRepo permissionRepo,
//                                        IUserService userService, PermissionMapper permissionMapper,
//                                        ItemValidator itemValidator, IAuthenticationService authenticationService) {
//        this.permissionRepo = permissionRepo;
//        this.userService = userService;
//        this.permissionMapper = permissionMapper;
//        this.itemValidator = itemValidator;
//        this.authenticationService = authenticationService;
//    }
//
//    @Override
//    public ItemPermissionResponse updatePermission(Long permissionId, PermissionRequest permissionRequest) {
//        log.info("update permission for permissionId: {}, permissionRequest: {}", permissionId, permissionRequest);
//        Permission permission = getPermissionByIdOrThrow(permissionId);
//        permission.setPermission(permissionRequest.getPermission());
//        permission = permissionRepo.save(permission);
//        return mapToItemPermissionResponse(permission);
//    }
//
//    @Override
//    public PageResponse<List<ItemPermissionResponse>> getPagePermissionByItemId(Long itemId, Pageable pageable) {
//        log.info("get page permission by resource id: {}", itemId);
//        Item resource = getResourceById(itemId);
//        // kiểm tra xem user hiện tại có permission với resource hiện tại hay k ?
//        validatePermissionManager(resource);
//        Page<Permission> pagePermission = permissionRepo.findAllByItemId(itemId, pageable);
//        return PaginationUtils.convertToPageResponse(pagePermission, pageable, this::mapToItemPermissionResponse);
//    }
//
//    @Override
//    public void deletePermissionById(Long permissionId) {
//        log.info("delete permission by permissionId: {}", permissionId);
//        permissionRepo.deleteById(permissionId);
//    }
//
//    @Override
//    public void deletePermissionByItemId(Long resourceId) {
//        log.info("delete permission by resource id: {}", resourceId);
//        permissionRepo.deleteByItemId(resourceId);
//    }
//
//
//    @Override
//    public void deleteByItemAndRecipientId(Long resourceId, Long recipientId) {
//        log.info("delete permission by resourceId: {}, recipientId: {}", resourceId, recipientId);
//        permissionRepo.deleteByItemIdAndRecipientId(resourceId, recipientId);
//    }
//
//    @Override
//    public void deletePermissionByItems(List<Long> resourceIds) {
//        log.info("delete permission by resourceIds: {}", resourceIds);
//        permissionRepo.deleteAllByItemIds(resourceIds);
//    }
//
//    protected Permission setPermission(Long resourceId, PermissionRequest permissionRequest) {
//        log.info("set permission for resourceId: {}, permission: {}, recipient id {}", resourceId, permissionRequest.getPermission(), permissionRequest.getRecipientId());
//        // kiem tra quyen da ton tai hay chua
//        validatePermissionNotExists(permissionRequest.getRecipientId(), resourceId);
//        Item resource = getResourceById(resourceId);
//        // validate đã thêm quyền này cho người này hay chưa ?
//        itemValidator.validateCurrentUserIsOwnerItem(resource);
//        // validate xem resource co bi xoa hay chua
//        itemValidator.validateItemNotDeleted(resource);
//        // validate xem người dùng có quyền tạo permission hay không
//        validatePermissionManager(resource);
//        Permission permission = mapToPermission(permissionRequest);
//        permission.setItem(resource);
//        return savePermission(permission);
//    }
//
//
//    protected void validatePermissionManager(Item resource) {
//        User curentUser = authenticationService.getCurrentUser();
//        if (resource.getOwner().getId().equals(curentUser.getId())) {
//            return;
//        }
//        if (!permissionRepo.isEditorPermission(resource.getId(), curentUser.getId())) {
//            throw new AccessDeniedException("Bạn không có quyền chỉnh sửa tài nguyên này");
//        }
//    }
//
//
//    protected Permission mapToPermission(PermissionRequest permissionRequest) {
//        Permission permission = new Permission();
//        permission.setPermission(permissionRequest.getPermission());
//        permission.setRecipient(getUserById(permissionRequest.getRecipientId()));
//        return permission;
//    }
//
//    protected ItemPermissionResponse mapToItemPermissionResponse(Permission permission) {
//        return permissionMapper.toItemPermissionResponse(permission);
//    }
//
//    /***
//     *  1 user chỉ có 1 quyền với 1 tài nguyên
//     * @param recipientId : id của user được cấp quyền
//     * @param resourceId : id tài nguyên được cấp quyền
//     */
//    protected void validatePermissionNotExists(Long recipientId, Long resourceId) {
//        if (permissionRepo.existsByRecipientIdAndItemId(recipientId, resourceId)) {
//            throw new InvalidDataException("Quyền đã tồn tại cho người dùng này trên tài nguyên này");
//        }
//    }
//
//    protected abstract Item getResourceById(Long resourceId);
//
//    protected Permission getPermissionByIdOrThrow(Long permissionId) {
//        return permissionRepo.findById(permissionId)
//                .orElseThrow(() -> new InvalidDataException("Không tìm thấy quyền với id này"));
//    }
//
//    protected Permission savePermission(Permission permission) {
//        return permissionRepo.save(permission);
//    }
//
//    protected User getUserById(Long id) {
//        return userService.getUserById(id);
//    }
//}
