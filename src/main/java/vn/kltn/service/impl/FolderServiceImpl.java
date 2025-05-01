package vn.kltn.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.kltn.dto.FolderContent;
import vn.kltn.dto.request.FolderRequest;
import vn.kltn.dto.response.FolderResponse;
import vn.kltn.entity.Document;
import vn.kltn.entity.Folder;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.repository.DocumentRepo;
import vn.kltn.repository.FolderRepo;
import vn.kltn.service.*;
import vn.kltn.util.ItemValidator;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@Slf4j(topic = "FOLDER_SERVICE")
public class FolderServiceImpl extends AbstractItemCommonService<Folder, FolderResponse> implements IFolderService {
    private final FolderRepo folderRepo;
    private final FolderCommonService folderCommonService;
    private final IFolderCreationService folderCreationService;
    private final IFolderMapperService folderMapperService;
    private final IFolderDeletionService folderDeletionService;
    private final IFolderRestorationService folderRestorationService;
    private final ItemValidator itemValidator;
    private final DocumentRepo documentRepo;

    public FolderServiceImpl(FolderRepo folderRepo, IAuthenticationService authenticationService, FolderCommonService folderCommonService, IFolderCreationService folderCreationService, IFolderMapperService folderMapperService, IFolderDeletionService folderDeletionService, IFolderRestorationService folderRestorationService, ItemValidator itemValidator, IPermissionInheritanceService permissionInheritanceService, IPermissionService permissionService, IPermissionValidatorService permissionValidatorService, DocumentRepo documentRepo) {
        super(authenticationService, folderCommonService, itemValidator, permissionInheritanceService, permissionValidatorService, permissionService);
        this.folderRepo = folderRepo;
        this.folderCommonService = folderCommonService;
        this.folderCreationService = folderCreationService;
        this.folderMapperService = folderMapperService;
        this.folderDeletionService = folderDeletionService;
        this.folderRestorationService = folderRestorationService;
        this.itemValidator = itemValidator;
        this.documentRepo = documentRepo;
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

    @Override
    protected Folder saveResource(Folder resource) {
        return folderRepo.save(resource);
    }

    @Override
    public void softDeleteFolderById(Long folderId) {
        log.info("Soft delete folder: folderId={}", folderId);
        Folder folder = getFolderByIdOrThrow(folderId);
        folderDeletionService.softDelete(folder);
    }

    @Override
    public FolderResponse restoreItemById(Long itemId) {
        Folder folder = folderRestorationService.restore(itemId);
        return folderMapperService.mapToResponse(folder);
    }

    @Override
    public Folder getItemByIdOrThrow(Long itemId) {
        return folderRepo.findById(itemId).orElseThrow(() -> {
            log.warn("Folder with id {} is not found", itemId);
            return new ResourceNotFoundException("Không tìm thấy thư mục");
        });
    }


    @Override
    protected void hardDeleteResource(Folder resource) {
        folderDeletionService.hardDelete(resource);
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
        itemValidator.validateCurrentUserIsOwnerItem(folder);
        itemValidator.validateItemNotDeleted(folder);
        folderMapperService.updateFolder(folder, folderRequest);
        return folderMapperService.mapToResponse(folderRepo.save(folder));
    }

    @Override
    public List<FolderContent> getAllContents(Long folderId, String currentPath) {
        List<FolderContent> results = new ArrayList<>();
        results.add(new FolderContent(currentPath + "/", true, null));
        // Lấy tất cả document của folder này
        List<Document> documents = documentRepo.findByParentId(folderId);
        for (Document doc : documents) {
            results.add(new FolderContent(
                    currentPath + "/" + doc.getName(),
                    false,
                    doc.getCurrentVersion().getBlobName()
            ));
        }

        // Lấy tất cả folder con
        List<Folder> subFolders = folderRepo.findByParentId(folderId);
        for (Folder sub : subFolders) {
            results.add(new FolderContent(
                    currentPath + "/" + sub.getName() + "/",
                    true,
                    null
            ));
            // đệ quy xuống folder con
            results.addAll(getAllContents(sub.getId(), currentPath + "/" + sub.getName()));
        }

        return results;
    }

}
