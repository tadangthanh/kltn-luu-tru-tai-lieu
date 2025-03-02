package vn.kltn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.kltn.entity.FileShare;

import java.util.Optional;

public interface FileShareRepo extends JpaRepository<FileShare, Long> {
    Optional<FileShare> findByToken(String token);

    Optional<FileShare> findByFileId(Long fileId);
}
