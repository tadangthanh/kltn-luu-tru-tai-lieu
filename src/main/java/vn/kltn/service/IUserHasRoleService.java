package vn.kltn.service;

import vn.kltn.entity.Role;
import vn.kltn.entity.User;
import vn.kltn.entity.UserHasRole;

public interface IUserHasRoleService {
    UserHasRole saveUserHasRole(User user, Role role);
}
