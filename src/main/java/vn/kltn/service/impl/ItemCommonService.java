package vn.kltn.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.kltn.entity.Item;
import vn.kltn.entity.User;
import vn.kltn.service.IAuthenticationService;
import vn.kltn.util.ResourceValidator;

@Service
@Slf4j(topic = "RESOURCE_COMMON_SERVICE")
@RequiredArgsConstructor
public class ItemCommonService {
    private final IAuthenticationService authenticationService;

    // T extends Item là khai báo tham số T thuôc kiểu Item
    public <T extends Item> void validateItemNotDeleted(T item) {
        log.info("validate resource not deleted");
        ResourceValidator.validateItemNotDeleted(item);
    }

    public <T extends Item> void validateCurrentUserIsOwnerItem(T item) {
        log.info("validate current user is owner resource");
        User currentUser = authenticationService.getCurrentUser();
        ResourceValidator.validateCurrentUserIsOwner(item, currentUser);
    }


}
