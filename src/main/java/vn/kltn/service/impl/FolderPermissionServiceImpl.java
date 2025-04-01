package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.kltn.dto.request.PermissionRequest;
import vn.kltn.dto.response.PermissionResponse;
import vn.kltn.entity.FileSystemEntity;
import vn.kltn.entity.Folder;
import vn.kltn.entity.Permission;
import vn.kltn.map.PermissionMapper;
import vn.kltn.repository.PermissionRepo;
import vn.kltn.service.IUserService;

@Service
@Transactional
@Slf4j(topic = "FOLDER_PERMISSION_SERVICE")
public class FolderPermissionServiceImpl extends AbstractPermissionService<Folder> implements IFolderPermissionService {
    private final FolderCommonService folderCommonService;

    protected FolderPermissionServiceImpl(PermissionRepo permissionRepo, IUserService userService, PermissionMapper permissionMapper, FolderCommonService folderCommonService, ResourceCommonService resourceCommonService) {
        super(permissionRepo, userService, permissionMapper, resourceCommonService);
        this.folderCommonService = folderCommonService;
    }


    @Override
    protected FileSystemEntity getResourceById(Long resourceId) {
        return folderCommonService.getFolderByIdOrThrow(resourceId);
    }


    @Override
    public PermissionResponse setPermissionResource(Long resourceId, PermissionRequest permissionRequest) {
        validatePermissionNotExistByRecipientAndResourceId( permissionRequest.getRecipientId(),resourceId);
        FileSystemEntity resource = getResourceById(resourceId);
        // validate đã thêm quyền này cho người này hay chưa ?
        resourceCommonService.validateCurrentUserIsOwnerResource(resource);
        // validate xem resource co bi xoa hay chua
        resourceCommonService.validateResourceNotDeleted(resource);
        Permission permission = mapToPermission(permissionRequest);
        permission.setResource(resource);
        return mapToPermissionResponse(savePermission(permission));
    }
}
