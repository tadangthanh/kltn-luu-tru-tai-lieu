package vn.kltn.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import vn.kltn.entity.FileShare;

import java.util.Optional;

public interface FileShareRepo extends JpaRepository<FileShare, Long> {
    Optional<FileShare> findByToken(String token);
    @Modifying
    @Transactional
    @Query("delete from FileShare fs where fs.file.id = ?1")
    void deleteByFileId(Long fileId);
    Optional<FileShare> findByFileId(Long fileId);

}
