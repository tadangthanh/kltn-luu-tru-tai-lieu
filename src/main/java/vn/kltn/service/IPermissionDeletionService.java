package vn.kltn.service;

public interface IPermissionDeletionService {
    void deleteByPermissionId(Long permissionId);

    void deleteByItemId(Long itemId);
}
