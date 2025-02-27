package vn.kltn.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import vn.kltn.entity.FileHasTag;

public interface FileHasTagRepo extends JpaRepository<FileHasTag, Long> {
    @Modifying
    @Transactional
    @Query("delete from FileHasTag fht where fht.file.id = ?1")
    void deleteByFileId(Long fileId);
}
