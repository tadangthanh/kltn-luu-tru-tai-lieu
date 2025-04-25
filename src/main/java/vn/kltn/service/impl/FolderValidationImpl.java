package vn.kltn.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vn.kltn.dto.request.FolderRequest;
import vn.kltn.entity.Folder;
import vn.kltn.entity.Item;
import vn.kltn.entity.User;
import vn.kltn.exception.ConflictResourceException;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.service.IAuthenticationService;
import vn.kltn.service.IFolderPermissionService;
import vn.kltn.service.IFolderValidation;

@Component
@Slf4j(topic = "FOLDER_VALIDATION_SERVICE")
@RequiredArgsConstructor
public class FolderValidationImpl implements IFolderValidation {
    private final FolderCommonService folderCommonService;
    private final IAuthenticationService authenticationService;
    private final IFolderPermissionService folderPermissionService;

    @Override
    public void validateConditionsToCreateFolder(FolderRequest folderRequest) {
        log.info("validate conditions to create folder with parentId {}", folderRequest.getFolderParentId());
        Folder folderParent = folderCommonService.getFolderByIdOrThrow(folderRequest.getFolderParentId());
        // kiem tra xem folder cha co ton tai hay khong
        if (folderParent.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Resource đã bị xóa");
        }
        User currentUser = authenticationService.getCurrentUser();
        if (!folderParent.getOwner().getId().equals(currentUser.getId())) {
            // neu ko phai la chu so huu thi kiem tra xem co phai la editor hay khong
            folderPermissionService.validateUserIsEditor(folderParent.getId(), currentUser.getId());
        }
    }

    @Override
    public void validateFolderDeleted(Folder folder) {
        if (folder.getDeletedAt() == null) {
            log.warn("Folder with id {} is not deleted", folder.getId());
            throw new ConflictResourceException("Thư mục chưa bị xóa");
        }
    }

    @Override
    public void validateFolderNotDeleted(Item resource) {
        if (resource.getDeletedAt() != null) {
            log.warn("Folder with id {} is deleted", resource.getId());
            throw new ConflictResourceException("Thư mục đã bị xóa");
        }
    }
}
