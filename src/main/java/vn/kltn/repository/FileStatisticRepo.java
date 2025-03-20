package vn.kltn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.kltn.entity.FileStatistic;

import java.util.Optional;

@Repository
public interface FileStatisticRepo extends JpaRepository<FileStatistic, Long> {
    boolean existsByFileId(Long fileId);

    void deleteByFileId(Long fileId);

    Optional<FileStatistic> findByFileId(Long fileId);
}
