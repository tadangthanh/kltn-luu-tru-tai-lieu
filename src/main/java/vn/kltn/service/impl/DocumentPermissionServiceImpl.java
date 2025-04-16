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
import vn.kltn.service.IDocumentIndexService;
import vn.kltn.service.IDocumentPermissionService;
import vn.kltn.service.IUserService;

import java.util.List;
import java.util.Set;

@Service
@Transactional
@Slf4j(topic = "DOCUMENT_PERMISSION_SERVICE")
public class DocumentPermissionServiceImpl extends AbstractPermissionService implements IDocumentPermissionService {
    private final DocumentCommonService documentCommonService;
    private final IDocumentIndexService documentIndexService;

    protected DocumentPermissionServiceImpl(
            PermissionRepo permissionRepo,
            IAuthenticationService authenticationService,
            IUserService userService,
            PermissionMapper permissionMapper,
            ResourceCommonService resourceCommonService,
            DocumentCommonService documentCommonService, IDocumentIndexService documentIndexService) {
        super(permissionRepo, userService, permissionMapper, resourceCommonService, authenticationService);
        this.documentCommonService = documentCommonService;
        this.documentIndexService = documentIndexService;
    }

    @Override
    public PermissionResponse setPermissionResource(Long resourceId, PermissionRequest permissionRequest) {
        PermissionResponse response=super.setPermissionResource(resourceId, permissionRequest);
        // update data trong elasticsearch
        updateIndexDocument(resourceId);
        return response;
    }

    @Override
    public void deletePermissionById(Long permissionId) {
        Permission permission = getPermissionByIdOrThrow(permissionId);
        Long resourceId = permission.getResource().getId();
        super.deletePermissionById(permissionId);
        // update data trong elasticsearch
        updateIndexDocument(resourceId);
    }

    @Override
    public void deleteByResourceAndRecipientId(Long resourceId, Long recipientId) {
        super.deleteByResourceAndRecipientId(resourceId, recipientId);
        // update data trong elasticsearch
        updateIndexDocument(resourceId);
    }

    @Override
    public void deletePermissionByResourceIds(List<Long> resourceIds) {
        super.deletePermissionByResourceIds(resourceIds);
        // update data trong elasticsearch
        for (Long resourceId : resourceIds) {
            updateIndexDocument(resourceId);
        }
    }

    @Override
    public void deletePermissionByResourceId(Long resourceId) {
        super.deletePermissionByResourceId(resourceId);
        // update data trong elasticsearch
        updateIndexDocument(resourceId);
    }

    @Override
    public PermissionResponse updatePermission(Long permissionId, PermissionRequest permissionRequest) {
        log.info("update permission for permissionId: {}, permissionRequest: {}", permissionId, permissionRequest);
        Permission permission = getPermissionByIdOrThrow(permissionId);
        permission.setPermission(permissionRequest.getPermission());
        permission = permissionRepo.save(permission);
        // update data trong elasticsearch
        updateIndexDocument(permission.getResource().getId());
        return mapToPermissionResponse(permission);
    }



    private void updateIndexDocument(Long resourceId) {
        Document document = documentCommonService.getDocumentByIdOrThrow(resourceId);
        documentIndexService.updateDocument(document);
    }

    @Override
    protected FileSystemEntity getResourceById(Long resourceId) {
        return documentCommonService.getDocumentByIdOrThrow(resourceId);
    }

    /***
     *  lay danh sach id user ma document nay chia se
     * @param documentId id document
     * @return danh sach id user mà document này chia sẻ
     */
    @Override
    public Set<Long> getUserIdsByDocumentShared(Long documentId) {
        return permissionRepo.findIdsUserSharedWithByResourceId(documentId);
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
        updateIndexDocument(resource.getId());
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
