package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import vn.kltn.common.RepoPermission;
import vn.kltn.entity.PermissionRepo;
import vn.kltn.entity.Role;
import vn.kltn.repository.PermissionRepositoryRepo;
import vn.kltn.repository.RoleRepo;

import java.util.Arrays;
import java.util.List;

import static vn.kltn.common.RepoPermission.*;

@Component
@RequiredArgsConstructor
@Transactional
public class DataInitializer implements CommandLineRunner {
    private final RoleRepo roleRepository;
    private final PermissionRepositoryRepo permissionRepositoryRepo;
    @Override
    public void run(String... args) throws Exception {
        List<Role> roles = this.initRole();
        roles.forEach((role) -> {
            if (!this.roleRepository.existsRoleByName(role.getName())) {
                this.roleRepository.save(role);
            }
        });
        initPermission();
    }
    private List<Role> initRole() {
        Role r1 = new Role();
        r1.setName("sysAdmin");
        Role r2 = new Role();
        r2.setName("admin");
        Role r3=new Role();
        r3.setName("manager");
        Role r4 = new Role();
        r4.setName("user");
        return List.of(r1, r2);
    }
    private void initPermission() {
        List<RepoPermission> permissions = Arrays.stream(values()).toList();
        for (RepoPermission permission : permissions) {
            if (!this.permissionRepositoryRepo.existsPermissionByPermission(permission)) {
                PermissionRepo permissionRepo = new PermissionRepo();
                permissionRepo.setPermission(permission);
                this.permissionRepositoryRepo.save(permissionRepo);
            }
        }
    }
}
