package vn.kltn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.kltn.common.RepoPermission;
import vn.kltn.entity.PermissionRepo;

import java.util.Optional;

@Repository
public interface PermissionRepositoryRepo extends JpaRepository<PermissionRepo, Long> {
    Optional<PermissionRepo> findByPermission(RepoPermission permissionRepository);
    boolean existsPermissionByPermission(RepoPermission permissionRepository);
}
