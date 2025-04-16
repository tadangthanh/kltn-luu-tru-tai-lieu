package vn.kltn.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.kltn.entity.Folder;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FolderRepo extends JpaRepository<Folder, Long>, JpaSpecificationExecutor<Folder> {
    @Query(value = """
            WITH RECURSIVE sub_folders AS (
                SELECT id FROM folder WHERE id = :folderId
                UNION ALL
                SELECT f.id FROM folder f
                                inner join file_system_entity fse ON f.id=fse.id
                                INNER JOIN sub_folders sf ON fse.parent_id = sf.id
            )
            SELECT id FROM sub_folders;
            """, nativeQuery = true)
    List<Long> findCurrentAndChildFolderIdsByFolderId(@Param("folderId") Long folderId);

    @Query(value = """
            WITH RECURSIVE sub_folders AS (
                -- Lấy folder gốc
                SELECT f.id
                FROM folder f
                WHERE f.id = :folderId
            
                UNION ALL
            
                -- Lấy folder con nếu chưa bị dừng bởi isCustomPermission = true
                SELECT f.id
                FROM folder f
                INNER JOIN file_system_entity fse ON f.id = fse.id
                INNER JOIN sub_folders sf ON fse.parent_id = sf.id
                INNER JOIN permission p ON fse.id = p.resource_id
                WHERE p.recipient_id = :userId
                AND f.deleted_at IS NULL
                AND p.is_custom_permission = false
                AND NOT EXISTS (
                    SELECT 1
                    FROM permission p2
                    WHERE p2.resource_id = fse.id
                    AND p2.recipient_id = :userId
                    AND p2.is_custom_permission = true
                )
            )
            
            -- Trả về các folder con hợp lệ
            SELECT id FROM sub_folders;
            """, nativeQuery = true)
        // lấy danh sách các id của folder con và folder hiện tại để cập nhật quyền (ko bao gom cac folder da bi xoa)
    List<Long> findAllFolderChildInheritedPermission(@Param("folderId") Long folderId, @Param("userId") Long userId);


    @Modifying
    @Transactional
    @Query("update Folder f set f.deletedAt= :deletedAt,f.permanentDeleteAt=:permanentDeletedAt where f.id in :folderIds")
    void setDeleteForFolders(@Param("folderIds") List<Long> folderIds, @Param("deletedAt") LocalDateTime deletedAt, @Param("permanentDeletedAt") LocalDateTime permanentDeletedAt);


}
