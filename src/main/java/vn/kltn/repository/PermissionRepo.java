package vn.kltn.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.kltn.entity.Permission;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface PermissionRepo extends JpaRepository<Permission, Long> {

    boolean existsByRecipientIdAndItemId(Long recipientId, Long itemId);

    @Modifying
    @Transactional
    @Query("UPDATE Permission p SET p.permission = :permission WHERE ((p.item.id IN :itemIds AND p.recipient.id = :recipientId) or (p.item.parent.id IN :itemIds)) and p.isCustomPermission = false")
        //update cả folder và document có parent id thuộc folderIdsForUpdatePermission
    void updateAllChildNotCustom(List<Long> itemIds, Long recipientId, vn.kltn.common.Permission permission);

    Page<Permission> findAllByItemId(Long itemId, Pageable pageable);

    @Query("""
                select count(p) > 0
                from Permission p
                where p.item.id = :itemId
                  and p.recipient.id = :recipientId
                  and p.permission = 'EDITOR'
            """)
    boolean isEditorPermission(@Param("itemId") Long itemId, @Param("recipientId") Long recipientId);


    @Query(value = """
            WITH RECURSIVE sub_folders AS (
                SELECT id FROM folder WHERE id = :folderId
                UNION ALL
                SELECT f.id FROM folder f
                                INNER JOIN item i ON f.id=i.id
                                INNER JOIN sub_folders sf ON i.parent_id = sf.id
                                where i.deleted_at is null AND NOT EXISTS(
                                                SELECT 1 FROM permission p where p.recipient_id = :recipientId
                                                                                         and p.item_id = i.id
                                            )
            )
            SELECT id FROM sub_folders where id != :folderId;
            """, nativeQuery = true)
        // lấy danh sách các id của folder con  (ko bao gom cac folder da bi xoa và folder hiện tại)
    List<Long> findSubFolderIdsEmptyPermission(@Param("folderId") Long folderId, @Param("recipientId") Long recipientId);


    void deleteByItemId(Long itemId);


    void deleteByItemIdAndRecipientId(Long itemId, Long recipientId);

    @Modifying
    @Transactional
    @Query("delete Permission p where p.item.id in ?1")
    void deleteAllByItemIds(List<Long> itemIds);

    @Query("SELECT p.recipient.id FROM Permission p WHERE p.item.id = ?1")
    Set<Long> findIdsUserSharedWithByItemId(Long itemId);

    @Query("SELECT p.item.id FROM Permission p WHERE p.recipient.id = ?1")
    Set<Long> findIdsDocumentByUserId(Long userId);

    @Modifying
    @Transactional
    @Query("delete from Permission p where p.item.id in ?1 and p.recipient.id = ?2")
    void deleteAllByItemIdInAndRecipientId(List<Long> folderChildIds, Long recipientId);

    Optional<Permission> findByItemIdAndRecipientId(Long itemId, Long recipientId);


    @Query("select p.item.id from Permission p where p.recipient.id = ?1")
    Set<Long> findItemIdsByRecipientId(Long recipientId);

    @Query("select p from Permission p where p.recipient.id = ?1 and p.item.id = ?2")
    Optional<Permission> findByRecipientAndItem(Long recipientId, Long itemId);

    @Query("select case when count(p) > 0 then true else false end from Permission p where p.item.id = ?1 and p.recipient.id = ?2 and p.permission = 'VIEWER'")
    boolean isViewerPermission(Long itemId, Long recipientId);

    @Modifying
    @Query("update Permission p set p.isHidden=false where p.item.id=?1 and p.recipient.id=?2")
    @Transactional
    void showItem(Long itemId, Long recipientId);
}
