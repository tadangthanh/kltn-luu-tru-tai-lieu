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
                SELECT f.id FROM folder f INNER JOIN sub_folders sf ON f.parent_id = sf.id
            )
            SELECT id FROM sub_folders;
            """, nativeQuery = true)
    List<Long> findCurrentAndChildFolderIdsByFolderId(@Param("folderId") Long folderId);

    @Modifying
    @Transactional
    @Query(value = """
            UPDATE folder SET deleted_at = :deletedAt WHERE id IN :folderIds
            """, nativeQuery = true)
    void updateDeletedAtForFolders(@Param("folderIds") List<Long> folderIds,@Param("deletedAt") LocalDateTime localDateTime);

}
