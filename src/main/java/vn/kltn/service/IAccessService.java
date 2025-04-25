package vn.kltn.service;

import org.springframework.data.domain.Pageable;
import vn.kltn.common.Permission;
import vn.kltn.dto.BaseDto;
import vn.kltn.dto.request.PermissionRequest;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.entity.AccessItem;

import java.util.List;
import java.util.Set;

public interface IAccessService<T extends AccessItem, R extends BaseDto> {
    R createAccess(Long resourceId, PermissionRequest permissionRequest);

    // update folder access cho 1 người cụ thể thì các folder,document trong nó cũng sẽ bị cập nhật theo
    R updateAccess(Long accessId, Permission newPermission);

    PageResponse<List<R>> getAccessByResource(Pageable pageable, String[] resources);

    Set<T> getAllByResourceId(Long resourceId);

    void deleteAccess(Long accessId);

    void validateUserIsEditor(Long resourceId, Long userId);

    void deleteAccessByResourceRecipientId(Long resourceId, Long recipientId);

    void deleteAccessByResourceId(Long resourceId);
}
