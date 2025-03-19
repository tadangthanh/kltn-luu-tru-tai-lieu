package vn.kltn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.kltn.common.RoleName;
import vn.kltn.entity.MemberRole;

import java.util.Optional;

@Repository
public interface MemberRoleRepo extends JpaRepository<MemberRole, Long> {
    Optional<MemberRole> findMemberRoleByName(RoleName roleName);

    boolean existsMemberRoleByName(RoleName name);
}
