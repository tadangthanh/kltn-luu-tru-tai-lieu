package vn.kltn.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.kltn.common.ItemType;
import vn.kltn.entity.Item;
import vn.kltn.entity.Permission;
import vn.kltn.entity.User;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.repository.FolderRepo;
import vn.kltn.repository.PermissionRepo;
import vn.kltn.service.IAuthenticationService;
import vn.kltn.service.IPermissionDeletionService;
import vn.kltn.service.IPermissionValidatorService;
import vn.kltn.service.event.publisher.PermissionEventPublisher;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@Transactional
@Slf4j(topic = "PERMISSION_DELETION_SERVICE")
@RequiredArgsConstructor
public class IPermissionDeletionServiceImpl implements IPermissionDeletionService {
    private final PermissionRepo permissionRepo;
    private final PermissionEventPublisher permissionEventPublisher;
    private final FolderRepo folderRepo;
    private final DocumentCommonService documentCommonService;
    private final IPermissionValidatorService permissionValidatorService;
    private final IAuthenticationService authenticationService;

    @Override
    public void deleteByPermissionId(Long permissionId) {
        log.info("delete permission by permissionId {}", permissionId);
        Permission permission = getPermissionByIdOrThrow(permissionId);
        ///  validate xem user co quyen xoa permission hay k, tao 1 service permission validator
        User currentUser = authenticationService.getCurrentUser();
        Item item = permission.getItem();
        permissionValidatorService.validatePermissionManager(item, currentUser);
        if (item.getItemType().equals(ItemType.DOCUMENT)) {
            permissionEventPublisher.publishDocumentUpdate(item.getId());
        } else {
            // la folder, se xoa permission cho tat ca cac item con (co the tao 1 service rieng)
            Long parentId = permission.getItem().getId();
            Long recipientId = permission.getRecipient().getId();

            // Lấy tất cả folder con và document con liên quan
            List<Long> folderIds = folderRepo.findCurrentAndChildFolderIdsByFolderId(parentId);
            List<Long> documentIds = documentCommonService.getDocumentChildIdsByFolderIds(folderIds);

            // Gộp tất cả resourceId để xóa permission
            List<Long> resourceIdsToDelete = new ArrayList<>(folderIds);
            resourceIdsToDelete.addAll(documentIds);
            // Xóa các permission liên quan đến user này cho các resource con
            permissionRepo.deleteAllByItemIdInAndRecipientId(resourceIdsToDelete, recipientId);
            permissionEventPublisher.publishDocumentsUpdate(Set.copyOf(documentIds));
        }
        // Xóa permission
        permissionRepo.delete(permission);
    }


    private Permission getPermissionByIdOrThrow(Long permissionId) {
        return permissionRepo.findById(permissionId).orElseThrow(() -> {
            log.error("Permission not found for permissionId: {}", permissionId);
            return new ResourceNotFoundException("Permission not found");
        });
    }

    @Override
    public void deleteByItemId(Long itemId) {
        log.info("delete permission by itemId : {}", itemId);
        permissionRepo.deleteByItemId(itemId);
        permissionEventPublisher.publishDocumentUpdate(itemId);
    }
}
