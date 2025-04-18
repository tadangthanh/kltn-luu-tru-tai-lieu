package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import vn.kltn.entity.*;
import vn.kltn.repository.PermissionRepo;
import vn.kltn.service.IAuthenticationService;
import vn.kltn.service.IDocumentValidator;
import vn.kltn.service.IPermissionInheritanceService;
import vn.kltn.service.event.MultipleDocumentsUpdatedEvent;
import vn.kltn.service.event.publisher.PermissionEventPublisher;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static vn.kltn.common.Permission.EDITOR;

@Service
@Transactional
@Slf4j(topic = "PERMISSION_INHERITANCE_SERVICE")
@RequiredArgsConstructor
public class PermissionInheritanceServiceImpl implements IPermissionInheritanceService {
    private final PermissionRepo permissionRepo;
    private final DocumentCommonService documentCommonService;
    private final ApplicationEventPublisher eventPublisher;
    private final FolderCommonService folderCommonService;
    private final PermissionEventPublisher permissionEventPublisher;
    private final IAuthenticationService authenticationService;
    private final IDocumentValidator documentValidator;
    @Override
    public void propagatePermissions(Long parentId, Permission permission) {
        createPermissionForChildResource(parentId, permission);
    }

    @Override
    public void inheritPermissionsFromParent(List<Document> documents) {
        log.info("Inheriting permissions for uploaded documents...");
        if (documents == null || documents.isEmpty()) return;

        User currentUser = authenticationService.getCurrentUser();

        for (Document document : documents) {
            if (document == null || document.getParent() == null) continue;

            documentValidator.validateDocumentNotDeleted(document);

            boolean isOwner = document.getParent().getOwner().getId().equals(currentUser.getId());
            inheritPermissions(document, isOwner);
        }
    }

    private void inheritPermissions(Document document, boolean isOwner) {
        Set<Permission> parentPermissions = document.getParent().getPermissions();
        if (parentPermissions == null || parentPermissions.isEmpty()) return;

        List<Permission> newPermissions = parentPermissions.stream()
                .map(permission -> permission.copyForResource(document))
                .collect(Collectors.toList());

        if (!isOwner) {
            // Editor tạo tài liệu → gán thêm quyền EDITOR cho chủ folder cha
            Permission extraPermission = createEditorPermissionFor(document, document.getParent().getOwner());
            newPermissions.add(extraPermission);
        }
        // Lọc bỏ quyền của người dùng đã sở hữu tài liệu
        newPermissions= newPermissions.stream()
                .filter(permission -> !permission.getRecipient().getId().equals(document.getOwner().getId()))
                .collect(Collectors.toList());

        permissionRepo.saveAll(newPermissions);
    }

    private Permission createEditorPermissionFor(Document document, User recipient) {
        return new Permission()
                .withRecipient(recipient)
                .withResource(document)
                .withPermission(EDITOR);
    }

    private void createPermissionForChildResource(Long parentResourceId, Permission permission) {
        User recipient = permission.getRecipient();
        // tạo permission cho  folder con
        List<Long> folderChildIds = createPermissionForFolderChild(parentResourceId, permission, recipient);
        // Thêm folder cha vào danh sách để xử lý các document con
        List<Long> folderIdsForDocument = new ArrayList<>(folderChildIds);
        folderIdsForDocument.add(parentResourceId);

        List<Long> documentChildIds = createPermissionForDocumentChild(folderIdsForDocument, permission, recipient);
        if (!documentChildIds.isEmpty()) {
            // update lai tron elasticsearch
            permissionEventPublisher.publishDocumentsUpdate(new HashSet<>(documentChildIds));
        }
    }

    private List<Long> createPermissionForDocumentChild(List<Long> parentIds, Permission permission, User recipient) {
        // Lấy danh sách các document con mà user chưa có permission
        List<Long> documentChildIds = documentCommonService.getDocumentChildIdsEmptyPermission(
                parentIds, recipient.getId());
        // Tạo danh sách Permission cho các document con
        List<Permission> permissionsForDocument = createPermissionForChild(documentChildIds, permission, documentCommonService::getDocumentByIdOrThrow);
        // Batch insert cho document (nếu có)
        if (!permissionsForDocument.isEmpty()) {
            permissionRepo.saveAllAndFlush(permissionsForDocument);
            // update lai tron elasticsearch
            eventPublisher.publishEvent(new MultipleDocumentsUpdatedEvent(this, new HashSet<>(documentChildIds)));
            return documentChildIds;
        }
        return new ArrayList<>();
    }

    private List<Long> createPermissionForFolderChild(Long parentId, Permission permission,
                                                      User recipient) {
        // Lấy danh sách các folder con (loại trừ folder cha) mà user chưa có permission
        List<Long> folderChildIds = permissionRepo.findSubFolderIdsEmptyPermission(
                parentId, recipient.getId());
        // Tạo danh sách Permission cho các folder con
        List<Permission> permissionsForFolder = createPermissionForChild(folderChildIds, permission, folderCommonService::getFolderByIdOrThrow);
        // Batch insert cho folder (nếu có)
        if (!permissionsForFolder.isEmpty()) {
            permissionRepo.saveAll(permissionsForFolder);
        }
        return folderChildIds;
    }

    private List<Permission> createPermissionForChild(List<Long> resourceIds,
                                                      Permission permission,
                                                      Function<Long, FileSystemEntity> getResourceByIdFunc) {
        return resourceIds.stream()
                .map(resourceId -> {
                    FileSystemEntity resource = getResourceByIdFunc.apply(resourceId);
                    return permission.copyForResource(resource);
                })
                .collect(Collectors.toList());
    }

}
