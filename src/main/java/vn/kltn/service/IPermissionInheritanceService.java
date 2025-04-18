package vn.kltn.service;


import vn.kltn.entity.Document;
import vn.kltn.entity.Permission;

import java.util.List;

public interface IPermissionInheritanceService {
    void propagatePermissions(Long parentId, Permission permission);

    void inheritPermissionsFromParent(List<Document> documents);
}
