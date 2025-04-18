package vn.kltn.service;


import vn.kltn.entity.Permission;

public interface IPermissionInheritanceService {
    void propagatePermissions(Long parentId, Permission permission);

}
