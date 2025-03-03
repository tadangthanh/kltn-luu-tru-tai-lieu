package vn.kltn.service;

import vn.kltn.entity.Role;

public interface IRoleService {
    Role findRoleByName(String name);
}
