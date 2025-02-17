package vn.kltn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.kltn.common.RepoPermission;
import vn.kltn.entity.PermissionRepository;

import java.util.Optional;

@Repository
public interface PermissionRepositoryRepo extends JpaRepository<PermissionRepository, Long> {
    Optional<PermissionRepository> findByPermission(RepoPermission permissionRepository);
}
