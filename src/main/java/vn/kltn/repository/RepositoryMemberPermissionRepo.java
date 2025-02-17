package vn.kltn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.kltn.entity.RepositoryMemberPermission;

@Repository
public interface RepositoryMemberPermissionRepo extends JpaRepository<RepositoryMemberPermission, Long> {
}
