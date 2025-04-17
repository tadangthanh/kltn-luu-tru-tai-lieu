package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import vn.kltn.dto.request.FolderRequest;
import vn.kltn.dto.response.FolderResponse;
import vn.kltn.entity.Folder;
import vn.kltn.exception.ConflictResourceException;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.repository.FolderRepo;
import vn.kltn.service.*;

@Service
@Transactional
@Slf4j(topic = "FOLDER_SERVICE")
public class FolderServiceImpl extends AbstractResourceService<Folder, FolderResponse> implements IFolderService {
    private final FolderRepo folderRepo;
    private final IDocumentService documentService;
    private final FolderCommonService folderCommonService;
    private final IFolderCreationService folderCreationService;
    private final IFolderMapperService folderMapperService;
    private final IFolderDeletionService folderDeletionService;
    private final IFolderRestorationService folderRestorationService;

    public FolderServiceImpl(@Qualifier("folderPermissionServiceImpl") AbstractPermissionService abstractPermissionService, IDocumentPermissionService documentPermissionService, FolderRepo folderRepo, IAuthenticationService authenticationService, IDocumentService documentService, FolderCommonService folderCommonService, IFolderPermissionService folderPermissionService, IFolderCreationService folderCreationService, IFolderMapperService folderMapperService, IFolderDeletionService folderDeletionService, IFolderRestorationService folderRestorationService) {
        super(documentPermissionService, folderPermissionService, authenticationService, abstractPermissionService, folderCommonService);
        this.folderRepo = folderRepo;
        this.documentService = documentService;
        this.folderCommonService = folderCommonService;
        this.folderCreationService = folderCreationService;
        this.folderMapperService = folderMapperService;
        this.folderDeletionService = folderDeletionService;
        this.folderRestorationService = folderRestorationService;
    }

    @Override
    public FolderResponse createFolder(FolderRequest folderRequest) {
        Folder folder = folderCreationService.createFolder(folderRequest);
        return mapToR(folder);
    }

    @Override
    public Folder getFolderByIdOrThrow(Long folderId) {
        return folderCommonService.getFolderByIdOrThrow(folderId);
    }

    private void validateFolderDeleted(Folder folder) {
        if (folder.getDeletedAt() == null) {
            log.warn("Folder with id {} is not deleted", folder.getId());
            throw new ConflictResourceException("Thư mục chưa bị xóa");
        }
    }

    @Override
    protected Folder saveResource(Folder resource) {
        return folderRepo.save(resource);
    }

    @Override
    protected void softDeleteResource(Folder folder) {
        folderDeletionService.softDelete(folder);
    }

    @Override
    public FolderResponse restoreResourceById(Long resourceId) {
        Folder folder = folderRestorationService.restore(resourceId);
        return folderMapperService.mapToResponse(folder);
    }

    @Override
    public Folder getResourceByIdOrThrow(Long resourceId) {
        return folderRepo.findById(resourceId).orElseThrow(() -> {
            log.warn("Folder with id {} is not found", resourceId);
            return new ResourceNotFoundException("Không tìm thấy thư mục");
        });
    }


    @Override
    protected void hardDeleteResource(Folder resource) {
        folderDeletionService.hardDelete(resource);
    }

    @Override
    protected Page<Folder> getPageResource(Pageable pageable) {
        log.info("get page folder");
        return folderRepo.findAll(pageable);
    }

    @Override
    protected Page<Folder> getPageResourceBySpec(Specification<Folder> spec, Pageable pageable) {
        return folderRepo.findAll(spec, pageable);
    }

    @Override
    protected FolderResponse mapToR(Folder resource) {
        return folderMapperService.mapToResponse(resource);
    }

    @Override
    public FolderResponse updateFolderById(Long folderId, FolderRequest folderRequest) {
        log.info("update folder with id: {}", folderId);
        Folder folder = getFolderByIdOrThrow(folderId);
        validateCurrentUserIsOwnerResource(folder);
        validateResourceNotDeleted(folder);
        folderMapperService.updateFolder(folder, folderRequest);
        return folderMapperService.mapToResponse(folderRepo.save(folder));
    }

}
