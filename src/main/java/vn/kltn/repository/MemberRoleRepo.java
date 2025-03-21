package vn.kltn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.kltn.common.RoleName;
import vn.kltn.entity.MemberRole;

import java.util.Optional;

@Repository
public interface MemberRoleRepo extends JpaRepository<MemberRole, Long> {
    Optional<MemberRole> findMemberRoleByName(RoleName roleName);

    @Query("SELECT CASE WHEN COUNT(mr) > 0 THEN TRUE ELSE FALSE END FROM MemberRole mr WHERE mr.id = ?1 and mr.name= 'ADMIN'")
    boolean isRoleAdminByRoleId(Long roleId);

    boolean existsMemberRoleByName(RoleName name);
}
