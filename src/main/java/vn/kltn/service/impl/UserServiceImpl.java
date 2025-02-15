package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.kltn.dto.request.UserRegister;
import vn.kltn.entity.Role;
import vn.kltn.entity.User;
import vn.kltn.entity.UserHasRole;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.map.UserMapper;
import vn.kltn.repository.RoleRepo;
import vn.kltn.repository.UserHasRoleRepo;
import vn.kltn.repository.UserRepo;
import vn.kltn.service.IMailService;
import vn.kltn.service.IUserService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements IUserService {
    private final UserRepo userRepo;
    private final RoleRepo roleRepo;
    private final IMailService gmailService;
    private final UserHasRoleRepo userHasRoleRepo;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;


    @Override
    public UserDetails loadUserByUsername(String email) {
        return userRepo.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }


    @Override
    public void register(UserRegister userRegister) {
        User user = userMapper.registerToUser(userRegister);
        user = userRepo.save(user);
        Role role = findRoleByName("user");
        // user tao moi se co role la user
        saveUserHasRole(user, role);
        gmailService.sendConfirmLink(user.getEmail(), user.getId(), UUID.randomUUID().toString());
    }

    private UserHasRole saveUserHasRole(User user, Role role) {
        UserHasRole userHasRole = new UserHasRole();
        userHasRole.setUser(user);
        userHasRole.setRole(role);
        return userHasRoleRepo.save(userHasRole);
    }

    private Role findRoleByName(String name) {
        return roleRepo.findRoleByName(name).orElseThrow(() -> {
            log.error("Role not found");
            return new ResourceNotFoundException("Role not found");
        });
    }
}
