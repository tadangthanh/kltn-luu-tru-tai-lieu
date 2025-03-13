package vn.kltn.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import vn.kltn.entity.RepoMember;

import java.util.Optional;
import java.util.Set;

public interface RepoMemberRepo extends JpaRepository<RepoMember, Long> {
    @Modifying
    @Transactional
    @Query("delete from RepoMember rm where rm.repo.id = ?1")
    void deleteByRepoId(Long repoId);

    @Query("select count(rm) from RepoMember rm where rm.repo.id = ?1")
    int countRepoMemberByRepoId(Long repoId);

    @Query("select rm from RepoMember rm where rm.repo.id = ?1 and rm.user.id = ?2 and rm.status = 'ACTIVE'")
    Optional<RepoMember> findRepoMemberActiveByRepoIdAndUserId(Long repoId, Long userId);

    @Query("select rm from RepoMember rm where rm.repo.id = ?1 and rm.user.id = ?2")
    Optional<RepoMember> findRepoMemberByRepoIdAndUserId(Long repoId, Long userId);

    @Query("select count(rm) from RepoMember rm where rm.repo.id = ?1 and rm.status = 'ACTIVE'")
    int countMemberActiveByRepoId(Long repoId);

    @Query("select case when count(rm) > 0 then true else false end from RepoMember rm where rm.repo.id = ?1 and rm.user.id = ?2 and rm.status = 'ACTIVE'")
    boolean isExistMemberActiveByRepoIdAndUserId(Long repoId, Long userId);

    Page<RepoMember> findAllByRepoId(Long repoId, Pageable pageable);

    Optional<RepoMember> getMemberActiveByRepoIdAndUserId(Long repoId, Long userId);

}
