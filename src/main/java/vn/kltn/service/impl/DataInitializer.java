package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import vn.kltn.entity.Role;
import vn.kltn.repository.RoleRepo;

import java.util.List;

@Component
@RequiredArgsConstructor
@Transactional
public class DataInitializer implements CommandLineRunner {
    private final RoleRepo roleRepository;

    @Override
    public void run(String... args) {
        // Tạo role hệ thống
        initRole();
    }

    private void initRole() {
        List<Role> roles = this.createRole();
        roles.forEach((role) -> {
            if (!this.roleRepository.existsRoleByName(role.getName())) {
                this.roleRepository.save(role);
            }
        });
    }

    private List<Role> createRole() {
        Role r1 = new Role();
        r1.setName("sysAdmin");
        Role r2 = new Role();
        r2.setName("admin");
        Role r3 = new Role();
        r3.setName("manager");
        Role r4 = new Role();
        r4.setName("user");
        return List.of(r1, r2);
    }
}
