package vn.kltn.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import vn.kltn.dto.request.PermissionRequest;
import vn.kltn.entity.Item;
import vn.kltn.entity.User;
import vn.kltn.exception.InvalidDataException;
import vn.kltn.repository.PermissionRepo;
import vn.kltn.service.IAuthenticationService;
import vn.kltn.service.IPermissionValidatorService;
import vn.kltn.util.ItemValidator;

@Service
@Slf4j(topic = "PERMISSION_VALIDATOR_SERVICE")
@RequiredArgsConstructor
public class PermissionValidatorServiceImpl implements IPermissionValidatorService {
    private final PermissionRepo permissionRepo;
    private final ItemGetterService itemGetterService;
    private final ItemValidator itemValidator;
    private final IAuthenticationService authenticationService;

    @Override
    public void validatePermissionItemNotExists(Long recipientId, Long itemId) {
        if (permissionRepo.existsByRecipientIdAndItemId(recipientId, itemId)) {
            throw new InvalidDataException("Quyền đã tồn tại cho người dùng này trên tài nguyên này");
        }
    }

    @Override
    public void validatePermissionEditor(Item item, User user) {
        if (item.getOwner().getId().equals(user.getId())) {
            return;
        }
        if (!permissionRepo.isEditorPermission(item.getId(), user.getId())) {
            throw new AccessDeniedException("Bạn không có quyền chỉnh sửa tài nguyên này");
        }
    }


    @Override
    public void validateAddPermissionRequest(Long itemId, PermissionRequest permissionRequest) {
        // Check if permission already exists
        validatePermissionItemNotExists(permissionRequest.getRecipientId(), itemId);

        Item item = itemGetterService.getItemByIdOrThrow(itemId);

        // Validate ownership
        itemValidator.validateCurrentUserIsOwnerItem(item);

        // Validate item not deleted
        itemValidator.validateItemNotDeleted(item);

        // Validate user has permission to manage permissions
        User currentUser = authenticationService.getCurrentUser();
        validatePermissionEditor(item, currentUser);
    }

    @Override
    public void validatePermissionViewer(Item item, User currentUser) {
        if (item.getOwner().getId().equals(currentUser.getId())) {
            return;
        }
        if (!permissionRepo.isViewerPermission(item.getId(), currentUser.getId())) {
            throw new AccessDeniedException("Bạn không có quyền truy cập tài nguyên này");
        }
    }
}
