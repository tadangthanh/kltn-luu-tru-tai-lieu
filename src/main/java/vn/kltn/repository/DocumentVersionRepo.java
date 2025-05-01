package vn.kltn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import vn.kltn.dto.LatestVersion;
import vn.kltn.entity.DocumentVersion;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentVersionRepo extends JpaRepository<DocumentVersion, Long> {

    @Query("""
                SELECT dv
                FROM DocumentVersion dv
                WHERE dv.document.id = :documentId
                ORDER BY dv.version DESC
                LIMIT 1
            """)
    DocumentVersion findLatestVersion(@Param("documentId") Long documentId);

    @Query("""
                SELECT dv FROM DocumentVersion dv
                WHERE dv.document.id = :documentId
                ORDER BY dv.version DESC
                OFFSET :limit
            """)
    List<DocumentVersion> findOldVersions(@Param("documentId") Long documentId, @Param("limit") int limit);

    List<DocumentVersion> findAllByDocumentId(Long documentId);

    @Query("SELECT MAX(dv.version) FROM DocumentVersion dv WHERE dv.document.id = :id")
    Optional<Integer> findLatestVersionNumber(Long id);


    @Query("SELECT new vn.kltn.dto.LatestVersion(v.document.id, MAX(v.version)) " +
           "FROM DocumentVersion v WHERE v.document.id IN :documentIds " +
           "GROUP BY v.document.id")
    List<LatestVersion> findLatestVersionNumbers(@Param("documentIds") List<Long> documentIds);

    @Query(value = """
                DELETE FROM document_version
                 WHERE id IN (
                     SELECT id
                       FROM (
                           SELECT id,
                                  ROW_NUMBER() OVER (
                                    PARTITION BY document_id
                                    ORDER BY version DESC
                                  ) AS rn
                             FROM document_version
                         ) t
                      WHERE t.rn > :keep
                  )
            """, nativeQuery = true)
    @Modifying
    @Transactional
    void deleteOldVersionsBeyondLimit(@Param("keep") int keep);

    @Modifying
    @Query("DELETE FROM DocumentVersion dv WHERE dv.expiredAt <= :now")
    void deleteExpiredVersions(@Param("now") LocalDateTime now);

    List<DocumentVersion> findAllByExpiredAtBefore(LocalDateTime time);

    @Query("SELECT dv FROM DocumentVersion dv WHERE dv.document.id IN :documentIds")
    List<DocumentVersion> findAllByDocumentIds(List<Long> documentIds);
}


