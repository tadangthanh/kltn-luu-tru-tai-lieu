package vn.kltn.service;

import vn.kltn.dto.request.PermissionRequest;
import vn.kltn.dto.response.PermissionResponse;
import vn.kltn.entity.Document;

import java.util.List;
import java.util.Set;

public interface IDocumentPermissionService extends IPermissionService {
    void inheritPermissions(List<Document> documents);

    PermissionResponse addPermission(Long resourceId, PermissionRequest permissionRequest);
    Set<Long> getDocumentIdsByUser(Long userId);

}
