package vn.kltn.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.kltn.entity.Resource;
import vn.kltn.entity.User;
import vn.kltn.service.IResourceCommonService;
import vn.kltn.util.ResourceValidator;

@Service
@Slf4j(topic = "RESOURCE_COMMON_SERVICE")
public abstract class AbstractResourceCommonService<T extends Resource> implements IResourceCommonService<T> {
    @Override
    public T getResourceById(Long resourceId) {
        return getResourceByIdOrThrow(resourceId);
    }

    @Override
    public void validateCurrentUserIsOwnerResource(T resource) {
        log.info("validateResourceNotDeleted");
        ResourceValidator.validateResourceNotDeleted(resource);
    }

    @Override
    public void validateResourceNotDeleted(T resource) {
        log.info("validateCurrentUserIsOwnerResource");
        User currentUser = getCurrentUser();
        ResourceValidator.validateCurrentUserIsOwner(resource, currentUser);
    }

    protected abstract User getCurrentUser();

    protected abstract T getResourceByIdOrThrow(Long resourceId);
}
