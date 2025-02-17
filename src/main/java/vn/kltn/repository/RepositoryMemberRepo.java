package vn.kltn.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import vn.kltn.entity.RepoMember;

import java.util.Optional;

@Repository
public interface RepositoryMemberRepo extends JpaRepository<RepoMember, Long> {
    Optional<RepoMember> findByRepositoryIdAndUserId(Long repositoryId, Long userId);

    @Transactional
    @Modifying
    void deleteByRepositoryIdAndUserId(Long repositoryId, Long userId);
}
