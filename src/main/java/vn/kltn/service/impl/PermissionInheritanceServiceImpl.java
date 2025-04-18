package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import vn.kltn.entity.FileSystemEntity;
import vn.kltn.entity.Permission;
import vn.kltn.entity.User;
import vn.kltn.repository.PermissionRepo;
import vn.kltn.service.IPermissionInheritanceService;
import vn.kltn.service.event.MultipleDocumentsUpdatedEvent;
import vn.kltn.service.event.publisher.PermissionEventPublisher;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    @Override
    public void propagatePermissions(Long parentId, Permission permission) {
        createPermissionForChildResource(parentId, permission);
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
