package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import vn.kltn.dto.request.PermissionRequest;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.dto.response.PermissionResponse;
import vn.kltn.entity.FileSystemEntity;
import vn.kltn.entity.Permission;
import vn.kltn.entity.Resource;
import vn.kltn.entity.User;
import vn.kltn.exception.InvalidDataException;
import vn.kltn.map.PermissionMapper;
import vn.kltn.repository.PermissionRepo;
import vn.kltn.repository.util.PaginationUtils;
import vn.kltn.service.IAuthenticationService;
import vn.kltn.service.IPermissionService;
import vn.kltn.service.IUserService;

import java.util.List;

import static vn.kltn.common.Permission.EDITOR;

@Service
@Transactional
@Slf4j
public abstract class AbstractPermissionService<T extends FileSystemEntity> implements IPermissionService<T> {
    protected final PermissionRepo permissionRepo;
    protected final IUserService userService;
    protected final PermissionMapper permissionMapper;
    protected final ResourceCommonService resourceCommonService;
    protected final IAuthenticationService authenticationService;

    protected AbstractPermissionService(PermissionRepo permissionRepo,
                                        IUserService userService, PermissionMapper permissionMapper,
                                        ResourceCommonService resourceCommonService, IAuthenticationService authenticationService) {
        this.permissionRepo = permissionRepo;
        this.userService = userService;
        this.permissionMapper = permissionMapper;
        this.resourceCommonService = resourceCommonService;
        this.authenticationService = authenticationService;
    }

    @Override
    public PermissionResponse setPermissionResource(Long resourceId, PermissionRequest permissionRequest) {
        log.info("set permission for resourceId: {}, permissionRequest: {}", resourceId, permissionRequest);
        validatePermissionNotExists(permissionRequest.getRecipientId(), resourceId);
        FileSystemEntity resource = getResourceById(resourceId);
        // validate đã thêm quyền này cho người này hay chưa ?
        resourceCommonService.validateCurrentUserIsOwnerResource(resource);
        // validate xem resource co bi xoa hay chua
        resourceCommonService.validateResourceNotDeleted(resource);
        Permission permission = mapToPermission(permissionRequest);
        permission.setResource(getResourceById(resourceId));
        return mapToPermissionResponse(savePermission(permission));
    }

    @Override
    public PermissionResponse updatePermission(Long permissionId, PermissionRequest permissionRequest) {
        log.info("update permission for permissionId: {}, permissionRequest: {}", permissionId, permissionRequest);
        Permission permission = getPermissionByIdOrThrow(permissionId);
        permission.setPermission(permissionRequest.getPermission());
        permission = permissionRepo.save(permission);
//        updatePermissionFoldersChild(permission);
        return mapToPermissionResponse(permission);
    }

    @Override
    public PageResponse<List<PermissionResponse>> getPagePermissionByResourceId(Long resourceId, Pageable pageable) {
        log.info("get page permission by resource id: {}", resourceId);
        Resource resource = getResourceById(resourceId);
        // kiểm tra xem user hiện tại có permission với resource hiện tại hay k ?
        validateEditorOrOwner(resource);
        Page<Permission> pagePermission = permissionRepo.findAllByResourceId(resourceId, pageable);
        return PaginationUtils.convertToPageResponse(pagePermission, pageable, this::mapToPermissionResponse);
    }

    @Override
    public void deletePermissionById(Long permissionId) {
        log.info("delete permission by permissionId: {}", permissionId);
        permissionRepo.deleteById(permissionId);
    }

    protected void validateEditorOrOwner(Resource resource) {
        User curentUser = authenticationService.getCurrentUser();
        if (resource.getOwner().getId().equals(curentUser.getId())) {
            return;
        }
        if (!permissionRepo.existsByResourceIdAndRecipientIdAndPermission(resource.getId(), curentUser.getId(), EDITOR)) {
            throw new AccessDeniedException("Bạn không có quyền chỉnh sửa tài nguyên này");
        }
    }


    protected Permission mapToPermission(PermissionRequest permissionRequest) {
        Permission permission = new Permission();
        permission.setPermission(permissionRequest.getPermission());
        permission.setRecipient(getUserById(permissionRequest.getRecipientId()));
        return permission;
    }

    protected PermissionResponse mapToPermissionResponse(Permission permission) {
        return permissionMapper.toPermissionResponse(permission);
    }

    protected void validatePermissionNotExists(Long recipientId, Long resourceId) {
        if (permissionRepo.existsByRecipientIdAndResourceId(recipientId, resourceId)) {
            throw new InvalidDataException("Quyền đã tồn tại cho người dùng này trên tài nguyên này");
        }
    }

    protected abstract FileSystemEntity getResourceById(Long resourceId);

    protected Permission getPermissionByIdOrThrow(Long permissionId) {
        return permissionRepo.findById(permissionId)
                .orElseThrow(() -> new InvalidDataException("Không tìm thấy quyền với id này"));
    }

    protected Permission savePermission(Permission permission) {
        return permissionRepo.save(permission);
    }

    protected User getUserById(Long id) {
        return userService.getUserById(id);
    }
}
