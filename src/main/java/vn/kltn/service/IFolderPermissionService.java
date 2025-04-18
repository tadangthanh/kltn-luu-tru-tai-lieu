package vn.kltn.service;

import vn.kltn.dto.request.PermissionRequest;
import vn.kltn.dto.response.PermissionResponse;

public interface IFolderPermissionService extends IPermissionService{
    PermissionResponse addPermission(Long resourceId, PermissionRequest permissionRequest);
}
