package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.kltn.dto.request.PermissionRequest;
import vn.kltn.dto.response.PermissionResponse;
import vn.kltn.entity.*;
import vn.kltn.map.PermissionMapper;
import vn.kltn.repository.PermissionRepo;
import vn.kltn.service.IAuthenticationService;
import vn.kltn.service.IDocumentPermissionService;
import vn.kltn.service.IUserService;
import vn.kltn.service.event.publisher.PermissionEventPublisher;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
@Slf4j(topic = "DOCUMENT_PERMISSION_SERVICE")
public class DocumentPermissionServiceImpl extends AbstractPermissionService implements IDocumentPermissionService {
    private final DocumentCommonService documentCommonService;
    private final PermissionEventPublisher permissionEventPublisher;

    protected DocumentPermissionServiceImpl(
            PermissionRepo permissionRepo,
            IAuthenticationService authenticationService,
            IUserService userService,
            PermissionMapper permissionMapper,
            ResourceCommonService resourceCommonService,
            DocumentCommonService documentCommonService, PermissionEventPublisher permissionEventPublisher) {
        super(permissionRepo, userService, permissionMapper, resourceCommonService, authenticationService);
        this.documentCommonService = documentCommonService;
        this.permissionEventPublisher = permissionEventPublisher;
    }

    @Override
    public PermissionResponse addPermission(Long resourceId, PermissionRequest permissionRequest) {
        Permission response = super.setPermission(resourceId, permissionRequest);
        // update data trong elasticsearch
        permissionEventPublisher.publishDocumentUpdate(resourceId);
        return mapToPermissionResponse(response);
    }

    @Override
    public void deletePermissionById(Long permissionId) {
        Permission permission = getPermissionByIdOrThrow(permissionId);
        Long resourceId = permission.getResource().getId();
        super.deletePermissionById(permissionId);
        // update data trong elasticsearch
        permissionEventPublisher.publishDocumentUpdate(resourceId);
    }

    @Override
    public void deleteByResourceAndRecipientId(Long resourceId, Long recipientId) {
        super.deleteByResourceAndRecipientId(resourceId, recipientId);
        // update data trong elasticsearch
        permissionEventPublisher.publishDocumentUpdate(resourceId);
    }

    @Override
    public void deletePermissionByResourceIds(List<Long> resourceIds) {
        super.deletePermissionByResourceIds(resourceIds);
        // update data trong elasticsearch
        permissionEventPublisher.publishDocumentsUpdate(new HashSet<>(resourceIds));
    }

    @Override
    public void deletePermissionByResourceId(Long resourceId) {
        super.deletePermissionByResourceId(resourceId);
        // update data trong elasticsearch
        permissionEventPublisher.publishDocumentUpdate(resourceId);
    }

    @Override
    public PermissionResponse updatePermission(Long permissionId, PermissionRequest permissionRequest) {
        log.info("update permission for permissionId: {}, permissionRequest: {}", permissionId, permissionRequest);
        Permission permission = getPermissionByIdOrThrow(permissionId);
        permission.setPermission(permissionRequest.getPermission());
        permission = permissionRepo.save(permission);
        return mapToPermissionResponse(permission);
    }


    @Override
    protected FileSystemEntity getResourceById(Long resourceId) {
        return documentCommonService.getDocumentByIdOrThrow(resourceId);
    }

    @Override
    public void inheritPermissions(List<Document> documents) {
        if (documents == null || documents.isEmpty()) return;

        User currentUser = authenticationService.getCurrentUser();

        for (Document document : documents) {
            Resource parent = document.getParent();
            if (parent == null) continue; // Bỏ qua nếu không có folder cha

            if (parent.getOwner().getId().equals(currentUser.getId())) {
                // Chủ sở hữu tạo → kế thừa bình thường
                inheritPermissionsForOwnerCreatedResource(document);
            } else {
                // Editor tạo → kế thừa và cấp quyền cho chủ folder cha
                inheritPermissionsForEditorCreatedResource(document, parent.getOwner().getId());
            }
        }
    }

    @Override
    protected void inheritPermission(Resource resource, Long ownerParentId) {
        super.inheritPermission(resource, ownerParentId);
        //update data trong elasticsearch
        permissionEventPublisher.publishDocumentUpdate(resource.getId());
    }

    /***
     *  lay danh sach document duoc chia se cho user nay
     * @param userId : id user
     * @return lay danh sach document duoc chia se cho user nay
     */
    @Override
    public Set<Long> getDocumentIdsByUser(Long userId) {
        return permissionRepo.findIdsDocumentByUserId(userId);
    }

}
