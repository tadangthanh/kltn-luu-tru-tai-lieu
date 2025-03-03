package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.kltn.entity.Role;
import vn.kltn.entity.User;
import vn.kltn.entity.UserHasRole;
import vn.kltn.repository.UserHasRoleRepo;
import vn.kltn.service.IUserHasRoleService;

@RequiredArgsConstructor
@Transactional
@Slf4j(topic = "USER-HAS-ROLE-SERVICE")
@Service
public class UserHasRoleServiceImpl implements IUserHasRoleService {
    private final UserHasRoleRepo userHasRoleRepo;

    @Override
    public UserHasRole saveUserHasRole(User user, Role role) {
        UserHasRole userHasRole = new UserHasRole();
        userHasRole.setUser(user);
        userHasRole.setRole(role);
        return userHasRoleRepo.save(userHasRole);
    }
}
