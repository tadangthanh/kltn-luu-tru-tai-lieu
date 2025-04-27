package vn.kltn.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vn.kltn.common.Permission;
import vn.kltn.entity.Item;
import vn.kltn.entity.User;
import vn.kltn.exception.InvalidDataException;
import vn.kltn.repository.PermissionRepo;
import vn.kltn.service.IAuthenticationService;

@RequiredArgsConstructor
@Component
@Slf4j(topic = "ITEM_VALIDATOR")
public class ItemValidator {
    private final IAuthenticationService authenticationService;
    private final PermissionRepo permissionRepo;

    /**
     * Kiểm tra người dùng hiện tại có phải chủ sở hữu của Item không.
     */
    public  void validateCurrentUserIsOwner(Item item, User currentUser) {
        if (item.getOwner() == null || !item.getOwner().getId().equals(currentUser.getId())) {
            throw new InvalidDataException("Không có quyền thực hiện hành động này");
        }
    }

    /**
     * Kiểm tra Item chưa bị xóa (deletedAt == null)
     */
    public  void validateItemNotDeleted(Item item) {
        if (item.getDeletedAt() != null) {
            throw new InvalidDataException("Resource đã bị xóa");
        }
    }

    /**
     * Kiểm tra Item đã bị xóa (deletedAt != null)
     */
    public  void validateItemDeleted(Item item) {
        if (item.getDeletedAt() == null) {
            throw new InvalidDataException("Resource chưa bị xóa");
        }

    }
    /**
     * Kiểm tra người dùng hiện tại co quyen su huu hay k
     */
    public  void validateCurrentUserIsOwnerItem(Item item) {
        User currentUser =authenticationService.getCurrentUser();
        if (!item.getOwner().getId().equals(currentUser.getId())) {
            throw new InvalidDataException("Bạn không có quyền thực hiện thao tác này");
        }
    }
    public  void validateCurrentUserIsOwnerOrEditorItem(Item item) {
        User currentUser = authenticationService.getCurrentUser();
        // neu la chu so huu thi ko can kiem tra quyen editor
        if (item.getOwner().getId().equals(currentUser.getId())) {
            return;
        }
        // nếu khong là chu so huu thi kiem tra quyen editor
        if(!permissionRepo.isEditorPermission(item.getId(),currentUser.getId())){
            log.info("User with id {} is not editor of resource with id {}", currentUser.getId(), item.getId());
            throw new InvalidDataException("Bạn không có quyền thực hiện thao tác này");
        }

    }
}
