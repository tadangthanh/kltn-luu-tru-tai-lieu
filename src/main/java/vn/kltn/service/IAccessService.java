package vn.kltn.service;

import vn.kltn.common.Permission;
import vn.kltn.dto.BaseDto;
import vn.kltn.dto.request.AccessRequest;

public interface IAccessService<R extends BaseDto> {
    R createAccess(Long resourceId, AccessRequest accessRequest);

    R updateAccess(Long accessId, Permission newPermission);

    void deleteAccess(Long accessId);
}
