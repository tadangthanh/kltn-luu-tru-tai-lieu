package vn.kltn.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import vn.kltn.entity.FileActivity;

public interface FileActivityRepo extends JpaRepository<FileActivity, Long> {
    @Modifying
    @Transactional
    @Query("delete from FileActivity fa where fa.file.id = ?1")
    void deleteByFileId(Long fileId);
}
