package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import vn.kltn.dto.request.PermissionRequest;
import vn.kltn.dto.response.ItemPermissionResponse;
import vn.kltn.entity.Item;
import vn.kltn.entity.Permission;
import vn.kltn.map.PermissionMapper;
import vn.kltn.repository.FolderRepo;
import vn.kltn.repository.PermissionRepo;
import vn.kltn.service.IAuthenticationService;
import vn.kltn.service.IFolderPermissionService;
import vn.kltn.service.IPermissionInheritanceService;
import vn.kltn.service.IUserService;
import vn.kltn.service.event.MultipleDocumentsUpdatedEvent;
import vn.kltn.util.ItemValidator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
@Transactional
@Slf4j(topic = "FOLDER_PERMISSION_SERVICE")
public class FolderPermissionServiceImpl extends AbstractPermissionService implements IFolderPermissionService {
    private final FolderCommonService folderCommonService;
    private final DocumentCommonService documentCommonService;
    private final FolderRepo folderRepo;
    private final ApplicationEventPublisher eventPublisher;
    private final IPermissionInheritanceService permissionInheritanceService;

    protected FolderPermissionServiceImpl(PermissionRepo permissionRepo, IUserService userService, PermissionMapper permissionMapper, IAuthenticationService authenticationService, FolderCommonService folderCommonService, DocumentCommonService documentCommonService, FolderRepo folderRepo, ApplicationEventPublisher eventPublisher, IPermissionInheritanceService permissionInheritanceService, ItemValidator itemValidator) {
        super(permissionRepo, userService, permissionMapper, itemValidator, authenticationService);
        this.folderCommonService = folderCommonService;
        this.documentCommonService = documentCommonService;
        this.folderRepo = folderRepo;
        this.eventPublisher = eventPublisher;
        this.permissionInheritanceService = permissionInheritanceService;
    }


    @Override
    protected Item getResourceById(Long resourceId) {
        return folderCommonService.getFolderByIdOrThrow(resourceId);
    }

    @Override
    public ItemPermissionResponse addPermission(Long resourceId, PermissionRequest permissionRequest) {
        Permission permission = super.setPermission(resourceId, permissionRequest);
        permissionInheritanceService.propagatePermissions(resourceId, permission);
        return mapToItemPermissionResponse(permission);
    }

    @Override
    public void deletePermissionById(Long permissionId) {
        Permission existingPermission = getPermissionByIdOrThrow(permissionId);
        Long parentId = existingPermission.getItem().getId();
        Long recipientId = existingPermission.getRecipient().getId();

        // Lấy tất cả folder con và document con liên quan
        List<Long> folderIds = folderRepo.findCurrentAndChildFolderIdsByFolderId(parentId);
        List<Long> documentIds = documentCommonService.getDocumentChildIdsByFolderIds(folderIds);

        // Gộp tất cả resourceId để xóa permission
        List<Long> resourceIdsToDelete = new ArrayList<>(folderIds);
        resourceIdsToDelete.addAll(documentIds);
        // Xóa chính permission gốc
        super.deletePermissionById(permissionId);

        // Xóa các permission liên quan đến user này cho các resource con
        permissionRepo.deleteAllByItemIdInAndRecipientId(resourceIdsToDelete, recipientId);
        eventPublisher.publishEvent(new MultipleDocumentsUpdatedEvent(this, new HashSet<>(documentIds)));
    }

    @Override
    public ItemPermissionResponse updatePermission(Long permissionId, PermissionRequest permissionRequest) {
        Permission permission = getPermissionByIdOrThrow(permissionId);
        permission.setPermission(permissionRequest.getPermission());
        permission = permissionRepo.save(permission);
        // cập nhật lại quyền cho các folder con và document con
        updateAllChildNotCustom(permission);
        return mapToItemPermissionResponse(permission);
    }

    // update các folder/document con mà không phải là custom permission
    private void updateAllChildNotCustom(Permission permission) {
        List<Long> folderIdsForUpdatePermission = folderCommonService.getAllFolderChildInheritedPermission(permission.getItem().getId(), permission.getRecipient().getId());
        // update cả folder và document có parent id thuộc folderIdsForUpdatePermission
        permissionRepo.updateAllChildNotCustom(folderIdsForUpdatePermission, permission.getRecipient().getId(), permission.getPermission());
    }
}
