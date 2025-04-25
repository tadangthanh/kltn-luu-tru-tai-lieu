package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.kltn.dto.request.FolderRequest;
import vn.kltn.entity.Folder;
import vn.kltn.repository.FolderRepo;
import vn.kltn.service.*;

@Service
@Transactional
@Slf4j(topic = "FOLDER_CREATION_SERVICE")
@RequiredArgsConstructor
public class FolderCreationServiceImpl implements IFolderCreationService {
    private final IAuthenticationService authenticationService;
    private final IFolderMapperService folderMapperService;
    private final FolderRepo folderRepo;
    private final FolderCommonService folderCommonService;
    private final IFolderPermissionService folderPermissionService;
    private final IFolderValidation folderValidation;

    @Override
    public Folder createFolder(FolderRequest request) {
        if (request.getFolderParentId() == null) {
            log.info("Creating folder with parentId is null");
            return createRootFolder(request);
        }
        log.info("Creating folder with parentId {}", request.getFolderParentId());
        folderValidation.validateConditionsToCreateFolder(request);
        Folder folderSaved= createChildFolder(request);
        folderPermissionService.inheritPermissions(folderSaved);
        return folderSaved;
    }

    private Folder createRootFolder(FolderRequest request) {
        Folder folder = folderMapperService.mapToFolder(request);
        folder.setOwner(authenticationService.getCurrentUser());
        return folderRepo.save(folder);
    }

    private Folder createChildFolder(FolderRequest request) {
        Folder folderParent = folderCommonService.getFolderByIdOrThrow(request.getFolderParentId());
        Folder folder = folderMapperService.mapToFolder(request);
        folder = folderRepo.save(folder);
        folder.setOwner(authenticationService.getCurrentUser());
        folder.setParent(folderParent);
        return folderRepo.save(folder);
    }

}
