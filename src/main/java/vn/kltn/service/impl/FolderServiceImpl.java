package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import vn.kltn.dto.request.FolderRequest;
import vn.kltn.dto.response.FolderResponse;
import vn.kltn.entity.Folder;
import vn.kltn.exception.ConflictResourceException;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.map.FolderMapper;
import vn.kltn.repository.FolderRepo;
import vn.kltn.service.*;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@Slf4j(topic = "FOLDER_SERVICE")
public class FolderServiceImpl extends AbstractResourceService<Folder, FolderResponse> implements IFolderService {
    private final FolderRepo folderRepo;
    private final IDocumentService documentService;
    private final FolderCommonService folderCommonService;
    @Value("${app.delete.document-retention-days}")
    private int documentRetentionDays;
    private final IFolderCreationService folderCreationService;
    private final IFolderMapperService folderMapperService;

    public FolderServiceImpl(@Qualifier("folderPermissionServiceImpl") AbstractPermissionService abstractPermissionService, IDocumentPermissionService documentPermissionService, FolderRepo folderRepo, IAuthenticationService authenticationService, IDocumentService documentService, FolderCommonService folderCommonService, IFolderPermissionService folderPermissionService, IFolderCreationService folderCreationService, IFolderMapperService folderMapperService) {
        super(documentPermissionService, folderPermissionService, authenticationService, abstractPermissionService, folderCommonService);
        this.folderRepo = folderRepo;
        this.documentService = documentService;
        this.folderCommonService = folderCommonService;
        this.folderCreationService = folderCreationService;
        this.folderMapperService = folderMapperService;
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
        // lay danh sach id cac folder va cac folder con can xoa
        List<Long> folderIdsDelete = folderRepo.findCurrentAndChildFolderIdsByFolderId(folder.getId());
        // update deletedAt cho cac folder va cac folder con
        folderRepo.setDeleteForFolders(folderIdsDelete, LocalDateTime.now(), LocalDateTime.now().plusDays(documentRetentionDays));
        // xoa document cua cac folder va cac folder con
        documentService.softDeleteDocumentsByFolderIds(folderIdsDelete);
    }

    @Override
    public FolderResponse restoreResourceById(Long resourceId) {
        Folder folder = getFolderByIdOrThrow(resourceId);
        validateFolderDeleted(folder);
        List<Long> folderIdsRestore = folderRepo.findCurrentAndChildFolderIdsByFolderId(resourceId);
        folderRepo.setDeleteForFolders(folderIdsRestore, null, null);
        List<Long> folderIds = folderRepo.findCurrentAndChildFolderIdsByFolderId(resourceId);
        documentService.restoreDocumentsByFolderIds(folderIds);
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
        log.info("hard delete folder with id: {}", resource.getId());
        // kiem tra xem folder da bi xoa chua
        validateFolderDeleted(resource);
        // lay danh sach id folder hien tai va cac folder con can xoa
        List<Long> folderIdsDelete = folderRepo.findCurrentAndChildFolderIdsByFolderId(resource.getId());
        // xoa cac document thuoc cac folder truoc do
        documentService.hardDeleteDocumentByFolderIds(folderIdsDelete);
        folderRepo.delete(resource);
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
        folderMapperService.updateFolder(folder,folderRequest);
        return folderMapperService.mapToResponse(folderRepo.save(folder));
    }

}
