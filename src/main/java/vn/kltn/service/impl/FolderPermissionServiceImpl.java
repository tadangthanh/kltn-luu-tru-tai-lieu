package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.kltn.dto.request.PermissionRequest;
import vn.kltn.dto.response.PermissionResponse;
import vn.kltn.entity.Document;
import vn.kltn.entity.FileSystemEntity;
import vn.kltn.entity.Folder;
import vn.kltn.entity.Permission;
import vn.kltn.map.PermissionMapper;
import vn.kltn.repository.PermissionRepo;
import vn.kltn.service.IAuthenticationService;
import vn.kltn.service.IUserService;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j(topic = "FOLDER_PERMISSION_SERVICE")
public class FolderPermissionServiceImpl extends AbstractPermissionService implements IFolderPermissionService {
    private final FolderCommonService folderCommonService;
    private final DocumentCommonService documentCommonService;

    protected FolderPermissionServiceImpl(PermissionRepo permissionRepo, IUserService userService, PermissionMapper permissionMapper, IAuthenticationService authenticationService, FolderCommonService folderCommonService, ResourceCommonService resourceCommonService, DocumentCommonService documentCommonService) {
        super(permissionRepo, userService, permissionMapper, resourceCommonService, authenticationService);
        this.folderCommonService = folderCommonService;
        this.documentCommonService = documentCommonService;
    }


    @Override
    protected FileSystemEntity getResourceById(Long resourceId) {
        return folderCommonService.getFolderByIdOrThrow(resourceId);
    }


    @Override
    // tạo permision cho folder thì các folder con và document con sẽ tự động được tạo permission
    public PermissionResponse setPermissionResource(Long resourceId, PermissionRequest permissionRequest) {
        PermissionResponse response = super.setPermissionResource(resourceId, permissionRequest);
        // insert permission cho cả các folder con
        insertPermissionForChild(resourceId, permissionRequest);
        return response;
    }

    private List<Permission> createPermissionForChild(List<Long> resourceIds, PermissionRequest permissionRequest, Function<Long, FileSystemEntity> getResourceByIdFunc) {
        return resourceIds.stream()
                .map(resourceId -> {
                    FileSystemEntity resource = getResourceByIdFunc.apply(resourceId);
                    Permission permissionChild = mapToPermission(permissionRequest);
                    permissionChild.setResource(resource);
                    return permissionChild;
                })
                .collect(Collectors.toList());
    }

    private void insertPermissionForChild(Long parentResourceId, PermissionRequest permissionRequest) {
        // Lấy danh sách các folder con (loại trừ folder cha) mà user chưa có permission
        List<Long> folderChildIds = permissionRepo.findSubFolderIdsEmptyPermission(
                parentResourceId, permissionRequest.getRecipientId());
        // Tạo danh sách Permission cho các folder con
        List<Permission> permissionsForFolder = createPermissionForChild(folderChildIds, permissionRequest, this::getResourceById);

        // Batch insert cho folder (nếu có)
        if (!permissionsForFolder.isEmpty()) {
            permissionRepo.saveAll(permissionsForFolder);
        }

        // Thêm folder cha vào danh sách để xử lý các document con
        List<Long> folderIdsForDocument = new ArrayList<>(folderChildIds);
        folderIdsForDocument.add(parentResourceId);

        // Lấy danh sách các document con mà user chưa có permission
        List<Long> documentChildIds = documentCommonService.getDocumentChildIdsEmptyPermission(
                folderIdsForDocument, permissionRequest.getRecipientId());
        // Tạo danh sách Permission cho các document con
        List<Permission> permissionsForDocument = createPermissionForChild(documentChildIds, permissionRequest,documentCommonService::getDocumentByIdOrThrow);

        // Batch insert cho document (nếu có)
        if (!permissionsForDocument.isEmpty()) {
            permissionRepo.saveAll(permissionsForDocument);
        }
    }


    @Override
    public PermissionResponse updatePermission(Long permissionId, PermissionRequest permissionRequest) {
        Permission permission = getPermissionByIdOrThrow(permissionId);
        permission.setPermission(permissionRequest.getPermission());
        permission = permissionRepo.save(permission);
        // cập nhật lại quyền cho các folder con và document con
        updateAllChildNotCustom(permission);
        return mapToPermissionResponse(permission);
    }

    // update các folder/document con mà không phải là custom permission
    private void updateAllChildNotCustom(Permission permission) {
        List<Long> folderIdsForUpdatePermission = folderCommonService.getAllFolderChildInheritedPermission(permission.getResource().getId(), permission.getRecipient().getId());
        // update cả folder và document có parent id thuộc folderIdsForUpdatePermission
        permissionRepo.updateAllChildNotCustom(folderIdsForUpdatePermission, permission.getRecipient().getId(), permission.getPermission());
    }
}
