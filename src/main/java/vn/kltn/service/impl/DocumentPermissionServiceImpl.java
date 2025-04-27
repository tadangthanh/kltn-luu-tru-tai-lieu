//package vn.kltn.service.impl;
//
//import jakarta.transaction.Transactional;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import vn.kltn.dto.request.PermissionRequest;
//import vn.kltn.dto.response.ItemPermissionResponse;
//import vn.kltn.entity.Document;
//import vn.kltn.entity.Item;
//import vn.kltn.entity.Permission;
//import vn.kltn.map.PermissionMapper;
//import vn.kltn.repository.PermissionRepo;
//import vn.kltn.service.IAuthenticationService;
//import vn.kltn.service.IDocumentPermissionService;
//import vn.kltn.service.IPermissionInheritanceService;
//import vn.kltn.service.IUserService;
//import vn.kltn.service.event.publisher.PermissionEventPublisher;
//import vn.kltn.util.ItemValidator;
//
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
//@Service
//@Transactional
//@Slf4j(topic = "DOCUMENT_PERMISSION_SERVICE")
//public class DocumentPermissionServiceImpl extends AbstractPermissionService implements IDocumentPermissionService {
//    private final DocumentCommonService documentCommonService;
//    private final PermissionEventPublisher permissionEventPublisher;
//    private final IPermissionInheritanceService permissionInheritanceService;
//
//    protected DocumentPermissionServiceImpl(
//            PermissionRepo permissionRepo,
//            IAuthenticationService authenticationService,
//            IUserService userService,
//            PermissionMapper permissionMapper,
//            DocumentCommonService documentCommonService, PermissionEventPublisher permissionEventPublisher, IPermissionInheritanceService permissionInheritanceService, ItemValidator itemValidator) {
//        super(permissionRepo, userService, permissionMapper,itemValidator, authenticationService);
//        this.documentCommonService = documentCommonService;
//        this.permissionEventPublisher = permissionEventPublisher;
//        this.permissionInheritanceService = permissionInheritanceService;
//    }
//
//    @Override
//    public ItemPermissionResponse addPermission(Long resourceId, PermissionRequest permissionRequest) {
//        Permission response = super.setPermission(resourceId, permissionRequest);
//        // update data trong elasticsearch
//        permissionEventPublisher.publishDocumentUpdate(resourceId);
//        return mapToItemPermissionResponse(response);
//    }
//
//    @Override
//    public void deletePermissionById(Long permissionId) {
//        Permission permission = getPermissionByIdOrThrow(permissionId);
//        Long resourceId = permission.getItem().getId();
//        super.deletePermissionById(permissionId);
//        // update data trong elasticsearch
//        permissionEventPublisher.publishDocumentUpdate(resourceId);
//    }
//
//    @Override
//    public void deleteByItemAndRecipientId(Long resourceId, Long recipientId) {
//        super.deleteByItemAndRecipientId(resourceId, recipientId);
//        // update data trong elasticsearch
//        permissionEventPublisher.publishDocumentUpdate(resourceId);
//    }
//
//    @Override
//    public void deletePermissionByItems(List<Long> resourceIds) {
//        super.deletePermissionByItems(resourceIds);
//        // update data trong elasticsearch
//        permissionEventPublisher.publishDocumentsUpdate(new HashSet<>(resourceIds));
//    }
//
//    @Override
//    public void deletePermissionByItemId(Long resourceId) {
//        super.deletePermissionByItemId(resourceId);
//        // update data trong elasticsearch
//        permissionEventPublisher.publishDocumentUpdate(resourceId);
//    }
//
//    @Override
//    public ItemPermissionResponse updatePermission(Long permissionId, PermissionRequest permissionRequest) {
//        log.info("update permission for permissionId: {}, permissionRequest: {}", permissionId, permissionRequest);
//        Permission permission = getPermissionByIdOrThrow(permissionId);
//        permission.setPermission(permissionRequest.getPermission());
//        permission = permissionRepo.save(permission);
//        return mapToItemPermissionResponse(permission);
//    }
//
//
//    @Override
//    protected Item getResourceById(Long resourceId) {
//        return documentCommonService.getDocumentByIdOrThrow(resourceId);
//    }
//
//    @Override
//    public void inheritPermissionsFromParent(List<Document> documents) {
//        permissionInheritanceService.inheritPermissionsFromParent(documents);
//    }
//
//
//    /***
//     *  lay danh sach document duoc chia se cho user nay
//     * @param userId : id user
//     * @return lay danh sach document duoc chia se cho user nay
//     */
//    @Override
//    public Set<Long> getDocumentIdsByUser(Long userId) {
//        return permissionRepo.findIdsDocumentByUserId(userId);
//    }
//
//}
