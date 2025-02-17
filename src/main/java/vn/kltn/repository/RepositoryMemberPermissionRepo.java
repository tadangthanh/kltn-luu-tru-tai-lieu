package vn.kltn.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import vn.kltn.entity.MemberPermission;

@Repository
public interface RepositoryMemberPermissionRepo extends JpaRepository<MemberPermission, Long> {
    @Modifying
    @Transactional
    void deleteByMemberId(Long memberId);

}
