package vn.kltn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.kltn.entity.RepoActivity;

public interface RepositoryActivityRepo extends JpaRepository<RepoActivity,Long> {
}
