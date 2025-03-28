package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import vn.kltn.dto.request.FolderRequest;
import vn.kltn.dto.response.FolderResponse;
import vn.kltn.entity.Folder;
import vn.kltn.entity.User;
import vn.kltn.exception.ConflictResourceException;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.map.FolderMapper;
import vn.kltn.repository.FolderRepo;
import vn.kltn.service.IAuthenticationService;
import vn.kltn.service.IDocumentService;
import vn.kltn.service.IFolderService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j(topic = "FOLDER_SERVICE")
public class FolderServiceImpl extends AbstractResourceService<Folder, FolderResponse> implements IFolderService {
    private final FolderMapper folderMapper;
    private final FolderRepo folderRepo;
    private final IAuthenticationService authenticationService;
    private final IDocumentService documentService;
    private final ResourceCommonService resourceCommonService;
    @Value("${app.delete.document-retention-days}")
    private int documentRetentionDays;

    @Override
    public FolderResponse createFolder(FolderRequest folderRequest) {
        if (folderRequest.getFolderParentId() == null) {
            log.info("Creating folder with parentId is null");
            Folder folderSaved = saveFolderWithoutParent(folderRequest);
            return mapToFolderResponse(folderSaved);
        }
        log.info("Creating folder with parentId {}", folderRequest.getFolderParentId());
        Folder folderSaved = saveFolderWithParent(folderRequest);
        return mapToFolderResponse(folderSaved);
    }

    private Folder saveFolderWithoutParent(FolderRequest folderRequest) {
        Folder folder = mapToFolder(folderRequest);
        folder.setOwner(authenticationService.getCurrentUser());
        return folderRepo.save(folder);
    }

    private Folder saveFolderWithParent(FolderRequest folderRequest) {
        Folder folderParent = getFolderByIdOrThrow(folderRequest.getFolderParentId());
        Folder folder = mapToFolder(folderRequest);
        folder = folderRepo.save(folder);
        folder.setOwner(authenticationService.getCurrentUser());
        folder.setParent(folderParent);
        return folderRepo.save(folder);
    }

    private Folder mapToFolder(FolderRequest folderRequest) {
        return folderMapper.toFolder(folderRequest);
    }

    private FolderResponse mapToFolderResponse(Folder folder) {
        FolderResponse folderResponse = folderMapper.toFolderResponse(folder);
        if (folder.getParent() != null) {
            folderResponse.setParentId(folder.getParent().getId());
        }
        return folderResponse;
    }

    @Override
    public Folder getFolderByIdOrThrow(Long folderId) {
        return resourceCommonService.getFolderByIdOrThrow(folderId);
    }

    private void validateFolderDeleted(Folder folder) {
        if (folder.getDeletedAt() == null) {
            log.warn("Folder with id {} is not deleted", folder.getId());
            throw new ConflictResourceException("Thư mục chưa bị xóa");
        }
    }


    @Override
    public FolderResponse restoreResourceById(Long resourceId) {
        Folder folder = getFolderByIdOrThrow(resourceId);
        validateFolderDeleted(folder);
        List<Long> folderIdsRestore = folderRepo.findCurrentAndChildFolderIdsByFolderId(resourceId);
        folderRepo.setDeleteForFolders(folderIdsRestore, null, null);
        List<Long> folderIds = folderRepo.findCurrentAndChildFolderIdsByFolderId(resourceId);
        documentService.restoreDocumentsByFolderIds(folderIds);
        return mapToFolderResponse(folder);
    }

    @Override
    public Folder getResourceByIdOrThrow(Long resourceId) {
        return folderRepo.findById(resourceId).orElseThrow(() -> {
            log.warn("Folder with id {} is not found", resourceId);
            return new ResourceNotFoundException("Không tìm thấy thư mục");
        });
    }

    @Override
    public void softDeleteResourceById(Long resourceId) {
        Folder folder = getFolderByIdOrThrow(resourceId);
        resourceCommonService.validateResourceNotDeleted(folder);
        // lay danh sach id cac folder va cac folder con can xoa
        List<Long> folderIdsDelete = folderRepo.findCurrentAndChildFolderIdsByFolderId(resourceId);
        // update deletedAt cho cac folder va cac folder con
        folderRepo.setDeleteForFolders(folderIdsDelete, LocalDateTime.now(), LocalDateTime.now().plusDays(documentRetentionDays));
        // xoa document cua cac folder va cac folder con
        documentService.softDeleteDocumentsByFolderIds(folderIdsDelete);
    }

    @Override
    public FolderResponse moveResourceToFolder(Long resourceId, Long folderId) {
        Folder folderToMove = getFolderByIdOrThrow(folderId);
        // folder cha va folder can di chuyen chua bi xoa
        resourceCommonService.validateResourceNotDeleted(folderToMove);
        Folder folderDestination = getFolderByIdOrThrow(folderId);
        resourceCommonService.validateResourceNotDeleted(folderDestination);
        folderToMove.setParent(folderDestination);
        return mapToFolderResponse(folderRepo.save(folderToMove));
    }

    @Override
    protected void deleteResource(Folder resource) {
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
        return folderRepo.findAll(pageable);
    }

    @Override
    protected Page<Folder> getPageResourceBySpec(Specification<Folder> spec, Pageable pageable) {
        return folderRepo.findAll(spec, pageable);
    }

    @Override
    protected FolderResponse mapToR(Folder resource) {
        FolderResponse folderResponse = folderMapper.toFolderResponse(resource);
        if (resource.getParent() != null) {
            folderResponse.setParentId(resource.getParent().getId());
        }
        return folderResponse;
    }

    @Override
    protected User getCurrentUser() {
        return authenticationService.getCurrentUser();
    }


    @Override
    public FolderResponse updateFolderById(Long folderId, FolderRequest folderRequest) {
        Folder folder = getFolderByIdOrThrow(folderId);
        validateCurrentUserIsOwnerResource(folder);
        validateResourceNotDeleted(folder);
        folderMapper.updateFolderFromRequest(folderRequest, folder);
        return mapToFolderResponse(folderRepo.save(folder));
    }
}
