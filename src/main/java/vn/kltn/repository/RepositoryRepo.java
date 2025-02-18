package vn.kltn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.kltn.entity.Repo;

import java.util.Optional;

@org.springframework.stereotype.Repository
public interface RepositoryRepo extends JpaRepository<Repo, Long> {
    Optional<Repo> findByName(String name);

}
