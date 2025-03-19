package vn.kltn.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.kltn.common.MemberStatus;
import vn.kltn.entity.RepoMember;

import java.util.Optional;

public interface RepoMemberRepo extends JpaRepository<RepoMember, Long> {

    @Query("select count(rm) from RepoMember rm where rm.repo.id = ?1")
    int countRepoMemberByRepoId(Long repoId);

    @Query("select rm from RepoMember rm where rm.repo.id = ?1 and rm.user.id = ?2 and rm.status = 'ACTIVE'")
    Optional<RepoMember> findRepoMemberActiveByRepoIdAndUserId(Long repoId, Long userId);

    @Query("select rm from RepoMember rm where rm.repo.id = ?1 and rm.user.id = ?2 and rm.status = ?3")
    Optional<RepoMember> findRepoMemberByRepoIdAndUserIdAndStatus(Long repoId, Long userId, MemberStatus status);

    @Query("select rm from RepoMember rm where rm.repo.id = ?1 and rm.user.id = ?2")
    Optional<RepoMember> findRepoMemberByRepoIdAndUserId(Long repoId, Long userId);

    @Query("select count(rm) from RepoMember rm where rm.repo.id = ?1 and rm.status = 'ACTIVE'")
    int countMemberActiveByRepoId(Long repoId);

    @Query("select case when count(rm) > 0 then true else false end from RepoMember rm where rm.repo.id = ?1 and rm.user.id = ?2 and rm.status = 'ACTIVE'")
    boolean isExistMemberActiveByRepoIdAndUserId(Long repoId, Long userId);

    Page<RepoMember> findAllByRepoId(Long repoId, Pageable pageable);

    Optional<RepoMember> getMemberActiveByRepoIdAndUserId(Long repoId, Long userId);

}
