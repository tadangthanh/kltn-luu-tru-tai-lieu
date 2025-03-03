package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.kltn.entity.Role;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.repository.RoleRepo;
import vn.kltn.service.IRoleService;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j(topic = "ROLE_SERVICE")
public class RoleServiceImpl implements IRoleService {
    private final RoleRepo roleRepo;

    @Override
    public Role findRoleByName(String name) {
        return roleRepo.findRoleByName(name).orElseThrow(() -> {
            log.error("Role not found, name: {}", name);
            return new ResourceNotFoundException("Role not found");
        });
    }
}
