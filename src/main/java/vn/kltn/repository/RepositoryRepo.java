package vn.kltn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.kltn.entity.Repository;

import java.util.Optional;

@org.springframework.stereotype.Repository
public interface RepositoryRepo extends JpaRepository<Repository, Long> {
    Optional<Repository> findByName(String name);

}
