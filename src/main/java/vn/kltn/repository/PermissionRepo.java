package vn.kltn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.kltn.entity.Permission;

public interface PermissionRepo extends JpaRepository<Permission, Long> {

    boolean existsByRecipientIdAndResourceId(Long recipientId, Long resourceId);
}
