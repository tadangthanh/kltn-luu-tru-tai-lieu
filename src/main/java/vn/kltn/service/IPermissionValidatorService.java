package vn.kltn.service;

import vn.kltn.dto.request.PermissionRequest;
import vn.kltn.entity.Item;
import vn.kltn.entity.User;

public interface IPermissionValidatorService {
    void validatePermissionItemNotExists(Long recipientId, Long itemId);
    void validatePermissionManager(Item item, User user);
    void validateAddPermissionRequest(Long itemId, PermissionRequest permissionRequest);
}
