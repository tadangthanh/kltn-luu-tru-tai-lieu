package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import vn.kltn.common.RoleName;
import vn.kltn.entity.MemberRole;
import vn.kltn.entity.Role;
import vn.kltn.entity.SubscriptionPlan;
import vn.kltn.repository.MemberRoleRepo;
import vn.kltn.repository.RoleRepo;
import vn.kltn.repository.SubscriptionPlanRepo;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
@Transactional
public class DataInitializer implements CommandLineRunner {
    private final RoleRepo roleRepository;
    private final MemberRoleRepo memberRoleRepo;
    @Value("${repo.max-total-storage-per-user}")
    private Long maxTotalStoragePerUser;
    @Value("${repo.max-members-per-repo-default}")
    private int maxMembersPerRepoDefault;
    @Value("${repo.max-repos-per-member-default}")
    private int maxReposPerMemberDefault;
    private final SubscriptionPlanRepo subscriptionPlanRepo;

    @Override
    public void run(String... args) {
        // Tạo role hệ thống
        initRole();
        // Tạo role cho member
        initMemberRole();
        // Tạo gói Free
        initFreePlan();
    }

    private void initRole() {
        List<Role> roles = this.createRole();
        roles.forEach((role) -> {
            if (!this.roleRepository.existsRoleByName(role.getName())) {
                this.roleRepository.save(role);
            }
        });
    }

    private void initMemberRole() {
        List<MemberRole> memberRoles = this.createMemberRole();
        memberRoles.forEach((memberRole) -> {
            if (!this.memberRoleRepo.existsMemberRoleByName(memberRole.getName())) {
                this.memberRoleRepo.save(memberRole);
            }
        });
    }

    private void initFreePlan() {
        SubscriptionPlan freePlan = this.createFreePlan();
        if (!this.subscriptionPlanRepo.existsByName(freePlan.getName())) {
            this.subscriptionPlanRepo.save(freePlan);
        }
    }

    private SubscriptionPlan createFreePlan() {
        SubscriptionPlan freePlan = new SubscriptionPlan();
        freePlan.setName("Free");
        freePlan.setMaxStorage(this.maxTotalStoragePerUser); // GB -> Byte
        freePlan.setMaxMembersPerRepo(this.maxMembersPerRepoDefault);
        freePlan.setMaxReposPerMember(this.maxReposPerMemberDefault);
        freePlan.setPrice(BigDecimal.valueOf(0));
        return freePlan;
    }

    private List<MemberRole> createMemberRole() {
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
