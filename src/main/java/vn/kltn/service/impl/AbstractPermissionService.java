package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.kltn.dto.request.PermissionRequest;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.dto.response.PermissionResponse;
import vn.kltn.entity.FileSystemEntity;
import vn.kltn.entity.Permission;
import vn.kltn.entity.User;
import vn.kltn.exception.InvalidDataException;
import vn.kltn.map.PermissionMapper;
import vn.kltn.repository.PermissionRepo;
import vn.kltn.repository.util.PaginationUtils;
import vn.kltn.service.IPermissionService;
import vn.kltn.service.IUserService;

import java.util.List;

@Service
@Transactional
@Slf4j
public abstract class AbstractPermissionService<T extends FileSystemEntity> implements IPermissionService<T> {
    protected final PermissionRepo permissionRepo;
    protected final IUserService userService;
    protected final PermissionMapper permissionMapper;
    protected final ResourceCommonService resourceCommonService;

    protected AbstractPermissionService(PermissionRepo permissionRepo,
                                        IUserService userService, PermissionMapper permissionMapper,
                                        ResourceCommonService resourceCommonService) {
        this.permissionRepo = permissionRepo;
        this.userService = userService;
        this.permissionMapper = permissionMapper;
        this.resourceCommonService = resourceCommonService;
    }

    @Override
    public PermissionResponse setPermissionResource(Long resourceId, PermissionRequest permissionRequest) {
        log.info("set permission for resourceId: {}, permissionRequest: {}", resourceId, permissionRequest);
        validatePermissionNotExistByRecipientAndResourceId(permissionRequest.getRecipientId(), resourceId);
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
        Page<Permission> pagePermission = permissionRepo.findAllByResourceId(resourceId, pageable);
        return PaginationUtils.convertToPageResponse(pagePermission, pageable, this::mapToPermissionResponse);
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

    protected void validatePermissionNotExistByRecipientAndResourceId(Long recipientId, Long resourceId) {
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
