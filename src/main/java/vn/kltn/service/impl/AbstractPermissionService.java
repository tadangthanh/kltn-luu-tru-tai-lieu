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
import java.util.Set;
import java.util.stream.Collectors;

import static vn.kltn.common.Permission.EDITOR;

@Service
@Transactional
@Slf4j
public abstract class AbstractPermissionService implements IPermissionService {
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
        // kiem tra quyen da ton tai hay chua
        validatePermissionNotExists(permissionRequest.getRecipientId(), resourceId);
        FileSystemEntity resource = getResourceById(resourceId);
        // validate đã thêm quyền này cho người này hay chưa ?
        resourceCommonService.validateCurrentUserIsOwnerResource(resource);
        // validate xem resource co bi xoa hay chua
        resourceCommonService.validateResourceNotDeleted(resource);
        // validate xem người dùng có quyền tạo permission hay không
        validateEditorOrOwner(resource);
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

    @Override
    public void deletePermissionByResourceId(Long resourceId) {
        log.info("delete permission by resource id: {}", resourceId);
        permissionRepo.deleteByResourceId(resourceId);
    }

    @Override
    public void validateUserIsEditor(Long resourceId, Long userId) {
        log.info("validate user is editor by resourceId: {}, userId: {}", resourceId, userId);
        if (!permissionRepo.existsByResourceIdAndRecipientIdAndPermission(resourceId, userId, EDITOR)) {
            log.warn("User with id {} is not editor of resource with id {}", userId, resourceId);
            throw new AccessDeniedException("Bạn không có quyền với tài nguyên này");
        }
    }

    @Override
    public void deleteByResourceAndRecipientId(Long resourceId, Long recipientId) {
        log.info("delete permission by resourceId: {}, recipientId: {}", resourceId, recipientId);
        permissionRepo.deleteByResourceIdAndRecipientId(resourceId, recipientId);
    }

    @Override
    public void deletePermissionByResourceIds(List<Long> resourceIds) {
        log.info("delete permission by resourceIds: {}", resourceIds);
        permissionRepo.deleteAllByResourceIds(resourceIds);
    }

    /***
     *  folder con sẽ kế thừa quyền truy cập từ folder cha ( trong truong hop chu so huu folder cha tao)
     *  trong trong hop nguoi tao folder la editor thi chu so huu se co quyen editor voi folder con ma thanh vien nay tao
     * @param resource : tài nguyên được tạo mới
     */
    @Override
    public void inheritPermissions(FileSystemEntity resource) {
        // folder cha của tài nguyên được tạo mới, lấy các permission từ folder cha và gán cho tài nguyên mới
        Resource folderParent = resource.getParent();
        User currentUser = authenticationService.getCurrentUser();
        if (folderParent.getOwner().getId().equals(currentUser.getId())) {
            // nếu chủ sở hữu của folder cha tạo thì folder được tạo sẽ kế thừa các permission từ folder cha
            inheritPermissionsForOwnerCreatedResource(resource);
        } else {
            // nếu do editor tạo thì folder được tạo sẽ kế thừa các permission từ folder cha và chủ sở hữu folder cha là editor
            inheritPermissionsForEditorCreatedResource(resource, folderParent.getOwner().getId());
        }
    }

    private void inheritPermissionsForOwnerCreatedResource(FileSystemEntity resource) {
        inheritPermission(resource, null);
    }

    private void inheritPermissionsForEditorCreatedResource(FileSystemEntity resource, Long ownerParentId) {
        inheritPermission(resource, ownerParentId);
    }

    /***
     * Thực hiện kế thừa quyền từ folder cha khi tạo folder con
     * @param resource: resource tạo mới
     * @param ownerParentId : id của chủ sở hữu folder cha
     */
    private void inheritPermission(FileSystemEntity resource, Long ownerParentId) {
        // Validate nếu resource bị xóa hoặc không có parent thì dừng lại
        resourceCommonService.validateResourceNotDeleted(resource);
        if (resource.getParent() == null) return;

        // Lấy danh sách permission từ folder cha
        Set<Permission> parentPermissions = getPermissionsByResourceId(resource.getParent().getId());
        if (parentPermissions.isEmpty()) return;

        // Tạo danh sách permission mới
        List<Permission> newPermissions = parentPermissions.stream()
                .map(permission -> new Permission(permission.getRecipient(), resource, permission.getPermission()))
                .collect(Collectors.toList());

        // Nếu là Editor, thêm quyền cho chủ sở hữu folder chứa cái folder mà edior đang tạo
        if (ownerParentId != null) {
            newPermissions.add(createPermissionForRecipientEditor(resource, ownerParentId));
        }

        // Lưu tất cả permissions một lần
        saveAllPermissions(newPermissions);
    }

    /***
     * Tạo permission editor cho chủ sở hữu folder cha với tài nguyên mà editor đang tạo
     * @param resource : tài nguyên mà editor đang tạo
     * @param recipientId : id của chủ sở hữu folder chứa tài nguyên mà editor đang tạo
     * @return: permission
     */
    protected Permission createPermissionForRecipientEditor(FileSystemEntity resource, Long recipientId) {
        Permission permission = new Permission();
        permission.setRecipient(getUserById(recipientId));
        permission.setResource(resource);
        permission.setPermission(EDITOR);
        return permission;
    }

    protected void saveAllPermissions(List<Permission> permissions) {
        if (permissions.isEmpty()) {
            return;
        }
        permissionRepo.saveAll(permissions);
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

    /***
     *  1 user chỉ có 1 quyền với 1 tài nguyên
     * @param recipientId : id của user được cấp quyền
     * @param resourceId : id tài nguyên được cấp quyền
     */
    protected void validatePermissionNotExists(Long recipientId, Long resourceId) {
        if (permissionRepo.existsByRecipientIdAndResourceId(recipientId, resourceId)) {
            throw new InvalidDataException("Quyền đã tồn tại cho người dùng này trên tài nguyên này");
        }
    }

    protected Set<Permission> getPermissionsByResourceId(Long resourceId) {
        return permissionRepo.findByResourceId(resourceId);
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
