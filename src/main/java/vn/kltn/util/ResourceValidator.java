package vn.kltn.util;

import vn.kltn.entity.Resource;
import vn.kltn.entity.User;
import vn.kltn.exception.InvalidDataException;

public class ResourceValidator {
    /**
     * Kiểm tra người dùng hiện tại có phải chủ sở hữu của resource không.
     */
    public static <T extends Resource> void validateCurrentUserIsOwner(T resource, User currentUser) {
        if (resource.getOwner() == null || !resource.getOwner().getId().equals(currentUser.getId())) {
            throw new InvalidDataException("Không có quyền thực hiện hành động này");
        }
    }

    /**
     * Kiểm tra resource chưa bị xóa (deletedAt == null)
     */
    public static <T extends Resource> void validateResourceNotDeleted(T resource) {
        if (resource.getDeletedAt() != null) {
            throw new InvalidDataException("Resource đã bị xóa");
        }
    }
}
