package vn.kltn.service;

import org.springframework.data.domain.Pageable;
import vn.kltn.dto.request.PermissionRequest;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.dto.response.PermissionResponse;
import vn.kltn.entity.FileSystemEntity;

import java.util.List;

public interface IPermissionService {
    PermissionResponse setPermissionResource(Long resourceId, PermissionRequest permissionRequest);

    PermissionResponse updatePermission(Long permissionId, PermissionRequest permissionRequest);

    void inheritPermissions(FileSystemEntity resource);

    void deletePermissionById(Long permissionId);


    PageResponse<List<PermissionResponse>> getPagePermissionByResourceId(Long resourceId, Pageable pageable);

    void validateUserIsEditor(Long resourceId, Long userId);

    void deleteByResourceAndRecipientId(Long resourceId, Long recipientId);

    void deletePermissionByResourceIds(List<Long> resourceIds);

    void deletePermissionByResourceId(Long resourceId);

}
