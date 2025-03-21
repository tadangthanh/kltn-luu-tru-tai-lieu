package vn.kltn.service;

import vn.kltn.common.RoleName;
import vn.kltn.entity.MemberRole;

public interface IMemberRoleService {
    MemberRole getRoleByName(RoleName roleName);

    MemberRole getRoleById(Long roleId);
    boolean isRoleAdminByRoleId(Long roleId);
}
