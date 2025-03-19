package vn.kltn.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.kltn.entity.Repo;

@org.springframework.stereotype.Repository
public interface RepositoryRepo extends JpaRepository<Repo, Long> {

    @Query("select r from  Repo r join Member rm on r.id = rm.repo.id where rm.user.id = ?1 and rm.status = 'ACTIVE'")
    Page<Repo> findAllByUserIdActive(Long userId, Pageable pageable);
}
