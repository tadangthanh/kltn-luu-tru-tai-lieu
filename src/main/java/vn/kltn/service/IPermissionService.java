package vn.kltn.service;

import vn.kltn.dto.request.PermissionRequest;
import vn.kltn.dto.response.PermissionResponse;
import vn.kltn.entity.FileSystemEntity;

public interface IPermissionService<T extends FileSystemEntity> {
    PermissionResponse setPermissionResource(Long resourceId, PermissionRequest permissionRequest);
}
