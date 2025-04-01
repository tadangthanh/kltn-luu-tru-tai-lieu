package vn.kltn.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import vn.kltn.entity.Permission;

import java.util.List;
import java.util.Optional;

public interface PermissionRepo extends JpaRepository<Permission, Long> {

    boolean existsByRecipientIdAndResourceId(Long recipientId, Long resourceId);

    @Modifying
    @Transactional
    @Query("UPDATE Permission p SET p.permission = :permission WHERE p.resource.id IN :resourceIds AND p.recipient.id = :recipientId and p.isCustomPermission = false")
    void updateAllChildNotCustom(List<Long> resourceIds, Long recipientId, vn.kltn.common.Permission permission);

    @Query("SELECT p FROM Permission p WHERE p.recipient.id = :recipientId AND p.resource.id = :resourceId")
    Optional<Permission> findByRecipientIdAndResourceId(Long recipientId, Long resourceId);
}
