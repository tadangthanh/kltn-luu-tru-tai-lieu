package vn.kltn.service;

import org.springframework.data.domain.Pageable;
import vn.kltn.common.Permission;
import vn.kltn.dto.BaseDto;
import vn.kltn.dto.request.AccessRequest;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.entity.AccessResource;

import java.util.List;
import java.util.Set;

public interface IAccessService<T extends AccessResource, R extends BaseDto> {
    R createAccess(Long resourceId, AccessRequest accessRequest);

    R updateAccess(Long accessId, Permission newPermission);

    PageResponse<List<R>> getAccessByResource(Pageable pageable, String[] resources);

    Set<T> getAllByResourceId(Long resourceId);

    void deleteAccess(Long accessId);

    void deleteAccessByResource(Long resourceId);
}
