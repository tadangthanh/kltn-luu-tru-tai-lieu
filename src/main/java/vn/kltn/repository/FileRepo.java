package vn.kltn.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.kltn.entity.File;

import java.time.LocalDateTime;

@Repository
public interface FileRepo extends JpaRepository<File, Long>, JpaSpecificationExecutor<File> {
    @Query("select f from File f join f.tags t where lower(t.tag.name) = lower(?2) and f.repo.id=?1")
    Page<File> findByRepoIdAndTagName(Long repoId, String tagName, Pageable pageable);

    @Query("SELECT f FROM File f WHERE f.repo.id = :repoId AND f.createdAt BETWEEN :startOfDay AND :endOfDay")
    Page<File> findFilesByRepoIdAndUploadDateRange(@Param("repoId") Long repoId,
                                                   @Param("startOfDay") LocalDateTime startOfDay,
                                                   @Param("endOfDay") LocalDateTime endOfDay,
                                                   Pageable pageable);

}
