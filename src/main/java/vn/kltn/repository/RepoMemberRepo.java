package vn.kltn.repository;

import jakarta.transaction.Transactional;
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

    @Query("select rm from RepoMember rm where rm.user.id = ?1 and rm.repo.id = ?2")
    Optional<RepoMember> findRepoMemberByUserIdAndRepoId(Long userId, Long repoId);

    @Query("select rm from RepoMember rm where rm.repo.id = ?1")
    Set<RepoMember> findAllByRepoId(Long repoId);

    @Query("select rm.sasToken from RepoMember rm where rm.user.id = ?1 and rm.repo.id = ?2")
    String getSasTokenByUserIdAndRepoId(Long userId, Long repoId);
}
