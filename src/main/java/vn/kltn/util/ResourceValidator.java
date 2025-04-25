package vn.kltn.util;

import vn.kltn.entity.Item;
import vn.kltn.entity.User;
import vn.kltn.exception.InvalidDataException;

public class ResourceValidator {
    /**
     * Kiểm tra người dùng hiện tại có phải chủ sở hữu của Item không.
     */
    public static <T extends Item> void validateCurrentUserIsOwner(T item, User currentUser) {
        if (item.getOwner() == null || !item.getOwner().getId().equals(currentUser.getId())) {
            throw new InvalidDataException("Không có quyền thực hiện hành động này");
        }
    }

    /**
     * Kiểm tra Item chưa bị xóa (deletedAt == null)
     */
    public static <T extends Item> void validateItemNotDeleted(T item) {
        if (item.getDeletedAt() != null) {
            throw new InvalidDataException("Resource đã bị xóa");
        }
    }
}
