package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import vn.kltn.dto.request.FolderRequest;
import vn.kltn.dto.response.FolderResponse;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.entity.Folder;
import vn.kltn.exception.ConflictResourceException;
import vn.kltn.map.FolderMapper;
import vn.kltn.repository.FolderRepo;
import vn.kltn.repository.specification.EntitySpecificationsBuilder;
import vn.kltn.repository.util.PaginationUtils;
import vn.kltn.service.IAuthenticationService;
import vn.kltn.service.IDocumentService;
import vn.kltn.service.IFolderService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j(topic = "FOLDER_SERVICE")
public class FolderServiceImpl implements IFolderService {
    private final FolderMapper folderMapper;
    private final FolderRepo folderRepo;
    private final IAuthenticationService authenticationService;
    private final IDocumentService documentService;
    private final FolderCommonService folderCommonService;
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
        folder.setUser(authenticationService.getCurrentUser());
        return folderRepo.save(folder);
    }

    private Folder saveFolderWithParent(FolderRequest folderRequest) {
        Folder folderParent = getFolderByIdOrThrow(folderRequest.getFolderParentId());
        Folder folder = mapToFolder(folderRequest);
        folder = folderRepo.save(folder);
        folder.setUser(authenticationService.getCurrentUser());
        folder.setParent(folderParent);
        return folderRepo.save(folder);
    }

    private Folder mapToFolder(FolderRequest folderRequest) {
        return folderMapper.toFolder(folderRequest);
    }

    private FolderResponse mapToFolderResponse(Folder folder) {
        FolderResponse folderResponse = folderMapper.toFolderResponse(folder);
        if (folder.getParent() != null) {
            folderResponse.setFolderParentId(folder.getParent().getId());
        }
        return folderResponse;
    }


    @Override
    public void softDeleteFolderById(Long folderId) {
        Folder folder = getFolderByIdOrThrow(folderId);
        folderCommonService.validateFolderNotDeleted(folder);
        // lay danh sach id cac folder va cac folder con can xoa
        List<Long> folderIdsDelete = folderRepo.findCurrentAndChildFolderIdsByFolderId(folderId);
        // update deletedAt cho cac folder va cac folder con
        folderRepo.setDeleteForFolders(folderIdsDelete, LocalDateTime.now(), LocalDateTime.now().plusDays(documentRetentionDays));
        // xoa document cua cac folder va cac folder con
        documentService.softDeleteDocumentsByFolderIds(folderIdsDelete);
    }

    @Override
    public Folder getFolderByIdOrThrow(Long folderId) {
        return folderCommonService.getFolderByIdOrThrow(folderId);
    }

    @Override
    public void hardDeleteFolderById(Long folderId) {
        Folder folder = getFolderByIdOrThrow(folderId);
        validateFolderDeleted(folder);
        List<Long> folderIdsDelete = folderRepo.findCurrentAndChildFolderIdsByFolderId(folderId);
        documentService.hardDeleteDocumentByFolderIds(folderIdsDelete);
        folderRepo.delete(folder);
    }

    @Override
    public FolderResponse restoreFolderById(Long folderId) {
        Folder folder = getFolderByIdOrThrow(folderId);
        validateFolderDeleted(folder);
        List<Long> folderIdsRestore = folderRepo.findCurrentAndChildFolderIdsByFolderId(folderId);
        folderRepo.setDeleteForFolders(folderIdsRestore, null, null);
        List<Long> folderIds = folderRepo.findCurrentAndChildFolderIdsByFolderId(folderId);
        documentService.restoreDocumentsByFolderIds(folderIds);
        return mapToFolderResponse(folder);
    }

    private void validateFolderDeleted(Folder folder) {
        if (folder.getDeletedAt() == null) {
            log.warn("Folder with id {} is not deleted", folder.getId());
            throw new ConflictResourceException("Thư mục chưa bị xóa");
        }
    }


    @Override
    public PageResponse<List<FolderResponse>> searchByCurrentUser(Pageable pageable, String[] folders) {
        log.info("search folder by current user");
        if (folders != null && folders.length > 0) {
            EntitySpecificationsBuilder<Folder> builder = new EntitySpecificationsBuilder<>();
            Pattern pattern = Pattern.compile("([a-zA-Z0-9_.]+?)([<:>~!])(.*)(\\p{Punct}?)(\\p{Punct}?)");
            for (String s : folders) {
                Matcher matcher = pattern.matcher(s);
                if (matcher.find()) {
                    builder.with(matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4), matcher.group(5));
                }
            }
            Specification<Folder> spec = builder.build();
            // nó trả trả về 1 spec mới
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.isNull(root.get("deletedAt")));
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("createdBy"), email));
            Page<Folder> docPage = folderRepo.findAll(spec, pageable);
            return PaginationUtils.convertToPageResponse(docPage, pageable, this::mapToFolderResponse);
        }
        return PaginationUtils.convertToPageResponse(folderRepo.findAll(pageable), pageable, this::mapToFolderResponse);
    }


    @Override
    public FolderResponse updateFolderById(Long folderId, FolderRequest folderRequest) {
        Folder folder = getFolderByIdOrThrow(folderId);
        folderMapper.updateFolderFromRequest(folderRequest, folder);
        return mapToFolderResponse(folderRepo.save(folder));
    }
}
