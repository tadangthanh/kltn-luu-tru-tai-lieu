package vn.kltn.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import vn.kltn.entity.FileActivity;

import java.time.LocalDateTime;

public interface FileActivityRepo extends JpaRepository<FileActivity, Long>, JpaSpecificationExecutor<FileActivity> {
    @Modifying
    @Transactional
    @Query("delete from FileActivity fa where fa.file.id = ?1")
    void deleteByFileId(Long fileId);

    @Query("SELECT fa FROM FileActivity fa WHERE fa.file.id = ?1 AND fa.createdAt BETWEEN ?2 AND ?3")
    Page<FileActivity> findActivityRepositoriesByFileIdAndCreatedAtRange(Long fileId, LocalDateTime startOfDay, LocalDateTime endOfDay, Pageable pageable);
}
