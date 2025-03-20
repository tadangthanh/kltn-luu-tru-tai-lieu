package vn.kltn.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.kltn.entity.FileStatistic;

import java.util.Optional;

@Repository
public interface FileStatisticRepo extends JpaRepository<FileStatistic, Long>, JpaSpecificationExecutor<FileStatistic> {
    boolean existsByFileId(Long fileId);

    void deleteByFileId(Long fileId);

    Optional<FileStatistic> findByFileId(Long fileId);

    @Query("SELECT fs FROM FileStatistic fs WHERE fs.file.id = :fileId")
    Page<FileStatistic> findAllByFileId(Long fileId, Pageable pageable);
}
