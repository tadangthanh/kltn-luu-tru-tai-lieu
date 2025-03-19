package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import vn.kltn.common.RoleName;
import vn.kltn.entity.MemberRole;
import vn.kltn.entity.Role;
import vn.kltn.repository.MemberRoleRepo;
import vn.kltn.repository.RoleRepo;

import java.util.List;

@Component
@RequiredArgsConstructor
@Transactional
public class DataInitializer implements CommandLineRunner {
    private final RoleRepo roleRepository;
    private final MemberRoleRepo memberRoleRepo;

    @Override
    public void run(String... args) {
        List<Role> roles = this.initRole();
        roles.forEach((role) -> {
            if (!this.roleRepository.existsRoleByName(role.getName())) {
                this.roleRepository.save(role);
            }
        });
        List<MemberRole> memberRoles = this.initMemberRole();
        memberRoles.forEach((memberRole) -> {
            if (!this.memberRoleRepo.existsMemberRoleByName(memberRole.getName())) {
                this.memberRoleRepo.save(memberRole);
            }
        });
    }

    private List<MemberRole> initMemberRole() {
        MemberRole admin = new MemberRole();
        admin.setName(RoleName.ADMIN);
        admin.setDescription("Toàn quyền với repository");
        MemberRole editor = new MemberRole();
        editor.setName(RoleName.EDITOR);
        editor.setDescription("Thêm, sửa, xem file");
        MemberRole viewer = new MemberRole();
        viewer.setName(RoleName.VIEWER);
        viewer.setDescription("Xem file");
        return List.of(admin, editor, viewer);
    }

    private List<Role> initRole() {
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
