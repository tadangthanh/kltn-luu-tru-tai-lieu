package vn.kltn.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.kltn.entity.Document;
import vn.kltn.entity.Folder;
import vn.kltn.entity.Permission;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface PermissionRepo extends JpaRepository<Permission, Long> {

    boolean existsByRecipientIdAndResourceId(Long recipientId, Long resourceId);

    @Modifying
    @Transactional
    @Query("UPDATE Permission p SET p.permission = :permission WHERE ((p.resource.id IN :resourceIds AND p.recipient.id = :recipientId) or (p.resource.parent.id IN :resourceIds)) and p.isCustomPermission = false")
        //update cả folder và document có parent id thuộc folderIdsForUpdatePermission
    void updateAllChildNotCustom(List<Long> resourceIds, Long recipientId, vn.kltn.common.Permission permission);

    Page<Permission> findAllByResourceId(Long resourceId, Pageable pageable);

    boolean existsByResourceIdAndRecipientIdAndPermission(Long resourceId, Long recipientId, vn.kltn.common.Permission permission);

    @Query(value = """
            WITH RECURSIVE sub_folders AS (
                SELECT id FROM folder WHERE id = :folderId
                UNION ALL
                SELECT f.id FROM folder f
                                INNER JOIN file_system_entity fse ON f.id=fse.id
                                INNER JOIN sub_folders sf ON fse.parent_id = sf.id
                                where f.deleted_at is null AND NOT EXISTS(
                                                SELECT 1 FROM permission p where p.recipient_id = :recipientId
                                                                                         and p.resource_id = fse.id
                                            )
            )
            SELECT id FROM sub_folders where id != :folderId;
            """, nativeQuery = true)
        // lấy danh sách các id của folder con  (ko bao gom cac folder da bi xoa và folder hiện tại)
    List<Long> findSubFolderIdsEmptyPermission(@Param("folderId") Long folderId, @Param("recipientId") Long recipientId);


    void deleteByResourceId(Long resourceId);


    void deleteByResourceIdAndRecipientId(Long resourceId, Long recipientId);

    @Modifying
    @Transactional
    @Query("delete Permission p where p.resource.id in ?1")
    void deleteAllByResourceIds(List<Long> resourceIds);

    Optional<Permission> findByResourceIdAndRecipientId(Long resourceId, Long recipientId);

    Set<Permission> findByResourceId(Long resourceId);

    @Query("SELECT p.recipient.id FROM Permission p WHERE p.resource.id = ?1")
    Set<Long> findIdsUserSharedWithByResourceId(Long resourceId);

    @Query("SELECT p.resource.id FROM Permission p WHERE p.recipient.id = ?1")
    Set<Long> findIdsDocumentByUserId(Long userId);
}
