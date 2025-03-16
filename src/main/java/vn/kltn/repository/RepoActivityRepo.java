package vn.kltn.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import vn.kltn.entity.File;
import vn.kltn.entity.RepoActivity;

import java.time.LocalDateTime;

public interface RepoActivityRepo extends JpaRepository<RepoActivity, Long> , JpaSpecificationExecutor<RepoActivity> {
    @Transactional
    @Modifying
    @Query("delete from RepoActivity ra where ra.repo.id = :repoId")
    void deleteByRepoId(Long repoId);

    Page<RepoActivity> findByRepoId(Long repoId, Pageable pageable);
    @Query("SELECT ra FROM RepoActivity ra WHERE ra.repo.id = :repoId AND ra.createdAt BETWEEN :startOfDay AND :endOfDay")
    Page<RepoActivity> findActiveRepositoriesByRepoIdAndCreatedAtRange(Long repoId, LocalDateTime startOfDay, LocalDateTime endOfDay, Pageable pageable);
}
