package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.kltn.common.RoleName;
import vn.kltn.entity.MemberRole;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.repository.MemberRoleRepo;
import vn.kltn.service.IMemberRoleService;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j(topic = "MEMBER_ROLE_SERVICE")
public class MemberRoleServiceImpl implements IMemberRoleService {
    private final MemberRoleRepo memberRoleRepo;

    @Override
    public MemberRole getRoleByName(RoleName roleName) {
        return memberRoleRepo.findMemberRoleByName(roleName).orElseThrow(() -> {
            log.warn("Role name: {} not found", roleName);
            return new ResourceNotFoundException("Role" + roleName + " not found");
        });
    }

    @Override
    public MemberRole getRoleById(Long roleId) {
        return memberRoleRepo.findById(roleId).orElseThrow(() -> {
            log.warn("Role id: {} not found", roleId);
            return new ResourceNotFoundException("Role id: " + roleId + " not found");
        });
    }

    @Override
    public boolean isRoleAdminByRoleId(Long roleId) {
        return memberRoleRepo.isRoleAdminByRoleId(roleId);
    }
}
