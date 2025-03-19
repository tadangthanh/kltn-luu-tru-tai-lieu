package vn.kltn.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.kltn.entity.Repo;

import java.util.Optional;
import java.util.Set;

@org.springframework.stereotype.Repository
public interface RepositoryRepo extends JpaRepository<Repo, Long> {
    Optional<Repo> findByName(String name);


    @Query("select r from Repo r where r.id in :repoIds")
    Page<Repo> findAllByRepoIdSet(Set<Long> repoIds, Pageable pageable);
}
