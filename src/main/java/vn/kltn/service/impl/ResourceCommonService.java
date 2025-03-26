package vn.kltn.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.kltn.entity.Document;
import vn.kltn.entity.Folder;
import vn.kltn.entity.Resource;
import vn.kltn.entity.User;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.repository.DocumentRepo;
import vn.kltn.repository.FolderRepo;
import vn.kltn.service.IAuthenticationService;
import vn.kltn.util.ResourceValidator;

@Service
@Slf4j(topic = "RESOURCE_COMMON_SERVICE")
@RequiredArgsConstructor
public class ResourceCommonService {
    private final IAuthenticationService authenticationService;
    private final FolderRepo folderRepo;
    private final DocumentRepo documentRepo;


    // T extends Resource là khai báo tham số T thuôc kiểu Resource
    public <T extends Resource> void validateResourceNotDeleted(T resource) {
        log.info("validateResourceNotDeleted");
        ResourceValidator.validateResourceNotDeleted(resource);
    }

    public <T extends Resource> void validateCurrentUserIsOwnerResource(T resource) {
        log.info("validateCurrentUserIsOwnerResource");
        User currentUser = authenticationService.getCurrentUser();
        ResourceValidator.validateCurrentUserIsOwner(resource, currentUser);
    }

    public Folder getFolderByIdOrThrow(Long folderId) {
        log.info("get folder by id or throw: folderId={}", folderId);
        return folderRepo.findById(folderId).orElseThrow(() -> {
            log.warn("Folder with id {} not found", folderId);
            return new ResourceNotFoundException("Không tìm thấy thư mục");
        });
    }

    public Document getDocumentByIdOrThrow(Long documentId) {
        return documentRepo.findById(documentId).orElseThrow(() -> {
            log.warn("Document with id {} not found", documentId);
            return new ResourceNotFoundException("Không tìm thấy document");
        });
    }

}
