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
import vn.kltn.entity.User;
import vn.kltn.exception.ConflictResourceException;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.map.FolderMapper;
import vn.kltn.repository.FolderRepo;
import vn.kltn.service.IAuthenticationService;
import vn.kltn.service.IDocumentPermissionService;
import vn.kltn.service.IDocumentService;
import vn.kltn.service.IFolderService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@Slf4j(topic = "FOLDER_SERVICE")
public class FolderServiceImpl extends AbstractResourceService<Folder, FolderResponse> implements IFolderService {
    private final FolderMapper folderMapper;
    private final FolderRepo folderRepo;
    private final IDocumentService documentService;
    private final ResourceCommonService resourceCommonService;
    private final FolderCommonService folderCommonService;
    @Value("${app.delete.document-retention-days}")
    private int documentRetentionDays;

    public FolderServiceImpl(@Qualifier("folderPermissionServiceImpl") AbstractPermissionService abstractPermissionService, IDocumentPermissionService documentPermissionService, FolderMapper folderMapper, FolderRepo folderRepo, IAuthenticationService authenticationService, IDocumentService documentService, ResourceCommonService resourceCommonService, FolderCommonService folderCommonService, IFolderPermissionService folderPermissionService) {
        super(documentPermissionService, folderPermissionService, authenticationService, abstractPermissionService, folderCommonService);
        this.folderMapper = folderMapper;
        this.folderRepo = folderRepo;
        this.documentService = documentService;
        this.resourceCommonService = resourceCommonService;
        this.folderCommonService = folderCommonService;
    }

    @Override
    public FolderResponse createFolder(FolderRequest folderRequest) {
        // tao folder khong co folder cha
        if (folderRequest.getFolderParentId() == null) {
            log.info("Creating folder with parentId is null");
            Folder folderSaved = saveFolderWithoutParent(folderRequest);
            return mapToFolderResponse(folderSaved);
        }
        // tao folder co folder cha
        log.info("Creating folder with parentId {}", folderRequest.getFolderParentId());
        validateConditionsToCreateFolder(folderRequest);
        Folder folderSaved = saveFolderWithParent(folderRequest);
        inheritedPermissionFromParent(folderSaved);
        return mapToFolderResponse(folderSaved);
    }

    /***
     * Khi tạo thư mục con thì sẽ kế thừa quyền truy cập từ thư mục cha
     *
     * @param folderSaved: folder cần kế thừa các permission từ folder cha của nó
     */
    private void inheritedPermissionFromParent(Folder folderSaved) {
        folderPermissionService.inheritPermissions(folderSaved);
    }


    private void validateConditionsToCreateFolder(FolderRequest folderRequest) {
        log.info("validate conditions to create folder with parentId {}", folderRequest.getFolderParentId());
        Folder folderParent = folderCommonService.getFolderByIdOrThrow(folderRequest.getFolderParentId());
        // kiem tra xem folder cha co ton tai hay khong
        validateResourceNotDeleted(folderParent);
        User currentUser = getCurrentUser();
        if (!folderParent.getOwner().getId().equals(currentUser.getId())) {
            // neu ko phai la chu so huu thi kiem tra xem co phai la editor hay khong
            folderPermissionService.validateUserIsEditor(folderParent.getId(), currentUser.getId());
        }
    }

    // tạo thư mục không có thư mục cha
    private Folder saveFolderWithoutParent(FolderRequest folderRequest) {
        Folder folder = mapToFolder(folderRequest);
        folder.setOwner(authenticationService.getCurrentUser());
        return folderRepo.save(folder);
    }

    // tạo thư mục có thư mục cha
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
        FolderResponse folderResponse = folderMapper.toFolderResponse(resource);
        if (resource.getParent() != null) {
            folderResponse.setParentId(resource.getParent().getId());
        }
        return folderResponse;
    }

    @Override
    public FolderResponse updateFolderById(Long folderId, FolderRequest folderRequest) {
        log.info("update folder with id: {}", folderId);
        Folder folder = getFolderByIdOrThrow(folderId);
        validateCurrentUserIsOwnerResource(folder);
        validateResourceNotDeleted(folder);
        folderMapper.updateFolderFromRequest(folderRequest, folder);
        return mapToFolderResponse(folderRepo.save(folder));
    }

}
