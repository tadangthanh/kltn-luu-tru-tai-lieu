package vn.kltn.service;

import vn.kltn.entity.Resource;

public interface IResourceCommonService<T extends Resource> {
    T getResourceById(Long resourceId);

    void validateResourceNotDeleted(T resource);

    void validateCurrentUserIsOwnerResource(T resource);
}
