package vn.kltn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.kltn.entity.RepositoryMember;

import java.util.Optional;

@Repository
public interface RepositoryMemberRepo extends JpaRepository<RepositoryMember, Long> {
    Optional<RepositoryMember> findByRepositoryIdAndUserId(Long repositoryId, Long userId);

}
