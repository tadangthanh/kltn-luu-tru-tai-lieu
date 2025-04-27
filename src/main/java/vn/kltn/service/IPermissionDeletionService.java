package vn.kltn.service;

import java.util.List;

public interface IPermissionDeletionService {
    void deleteByPermissionId(Long permissionId);

    void deletePermissionByItems(List<Long> itemIds);

    void deleteByItemAndRecipientId(Long resourceId, Long recipientId);

    void deleteByItemId(Long itemId);
}
