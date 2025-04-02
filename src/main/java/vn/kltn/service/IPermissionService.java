package vn.kltn.service;

import org.springframework.data.domain.Pageable;
import vn.kltn.dto.request.PermissionRequest;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.dto.response.PermissionResponse;
import vn.kltn.entity.FileSystemEntity;

import java.util.List;

public interface IPermissionService<T extends FileSystemEntity> {
    PermissionResponse setPermissionResource(Long resourceId, PermissionRequest permissionRequest);

    PermissionResponse updatePermission(Long permissionId, PermissionRequest permissionRequest);


    PageResponse<List<PermissionResponse>> getPagePermissionByResourceId(Long resourceId, Pageable pageable);
}
