package vn.kltn.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import vn.kltn.entity.RepoActivity;

public interface RepositoryActivityRepo extends JpaRepository<RepoActivity,Long> {
    @Transactional
    @Modifying
    @Query("delete from RepoActivity ra where ra.repo.id = :repoId")
    void deleteByRepoId(Long repoId);
}
