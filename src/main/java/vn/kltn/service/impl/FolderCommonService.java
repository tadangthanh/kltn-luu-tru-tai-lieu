package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.kltn.entity.Folder;
import vn.kltn.entity.User;
import vn.kltn.exception.InvalidDataException;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.repository.FolderRepo;
import vn.kltn.service.IAuthenticationService;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j(topic = "FOLDER_COMMON_SERVICE")
public class FolderCommonService {
    private final FolderRepo folderRepo;
    private final IAuthenticationService authenticationService;

    public Folder getFolderByIdOrThrow(Long folderId) {
        log.info("get folder by id or throw: folderId={}", folderId);
        return folderRepo.findById(folderId).orElseThrow(() -> {
            log.warn("Folder with id {} not found", folderId);
            return new ResourceNotFoundException("Không tìm thấy thư mục");
        });
    }

    public void validateCurrentUserIsOwnerFolder(Folder folder) {
        log.info("validate current user is owner folder");
        User currentUser = authenticationService.getCurrentUser();
        if (!folder.getOwner().getId().equals(currentUser.getId())) {
            log.warn("Current user is not owner folder: {}", folder.getId());
            throw new InvalidDataException("Không có quyền thực hiện hành động này");
        }
    }

    public void validateFolderNotDeleted(Folder folder) {
        log.info("validate folder not deleted");
        if (folder.getDeletedAt() != null) {
            log.warn("Folder is deleted: {}", folder.getId());
            throw new InvalidDataException("Thư mục đã bị xóa");
        }
    }


}
