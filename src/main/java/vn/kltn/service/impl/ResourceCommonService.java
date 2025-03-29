package vn.kltn.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.kltn.entity.Resource;
import vn.kltn.entity.User;
import vn.kltn.service.IAuthenticationService;
import vn.kltn.util.ResourceValidator;

@Service
@Slf4j(topic = "RESOURCE_COMMON_SERVICE")
@RequiredArgsConstructor
public class ResourceCommonService {
    private final IAuthenticationService authenticationService;

    // T extends Resource là khai báo tham số T thuôc kiểu Resource
    public <T extends Resource> void validateResourceNotDeleted(T resource) {
        log.info("validate resource not deleted");
        ResourceValidator.validateResourceNotDeleted(resource);
    }

    public <T extends Resource> void validateCurrentUserIsOwnerResource(T resource) {
        log.info("validate current user is owner resource");
        User currentUser = authenticationService.getCurrentUser();
        ResourceValidator.validateCurrentUserIsOwner(resource, currentUser);
    }


}
