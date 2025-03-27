package vn.kltn.service;

import org.springframework.data.domain.Pageable;
import vn.kltn.common.Permission;
import vn.kltn.dto.BaseDto;
import vn.kltn.dto.request.AccessRequest;
import vn.kltn.dto.response.PageResponse;

import java.util.List;

public interface IAccessService<R extends BaseDto> {
    R createAccess(Long resourceId, AccessRequest accessRequest);

    R updateAccess(Long accessId, Permission newPermission);

    PageResponse<List<R>> getAccessByResource(Pageable pageable,String[] resources);

    void deleteAccess(Long accessId);
}
