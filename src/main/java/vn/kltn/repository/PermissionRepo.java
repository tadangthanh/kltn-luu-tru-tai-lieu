package vn.kltn.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import vn.kltn.entity.Permission;

import java.util.List;

public interface PermissionRepo extends JpaRepository<Permission, Long> {

    boolean existsByRecipientIdAndResourceId(Long recipientId, Long resourceId);
//
//    @Modifying
//    @Transactional
//    @Query("UPDATE Permission p SET p.permission = :permission WHERE p.resource.id IN :resourceIds AND p.recipient.id = :recipientId and p.isCustomPermission = false")
//    void updateAllChildNotCustom(List<Long> resourceIds, Long recipientId, vn.kltn.common.Permission permission);


    @Modifying
    @Transactional
    @Query("UPDATE Permission p SET p.permission = :permission WHERE ((p.resource.id IN :resourceIds AND p.recipient.id = :recipientId) or (p.resource.parent.id IN :resourceIds)) and p.isCustomPermission = false")
    void updateAllChildNotCustom(List<Long> resourceIds, Long recipientId, vn.kltn.common.Permission permission);

    Page<Permission> findAllByResourceId(Long resourceId, Pageable pageable);

    boolean existsByResourceIdAndRecipientIdAndPermission(Long resourceId, Long recipientId, vn.kltn.common.Permission permission);
}
