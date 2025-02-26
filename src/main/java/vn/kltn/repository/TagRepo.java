package vn.kltn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.kltn.entity.Tag;

import java.util.Optional;

public interface TagRepo extends JpaRepository<Tag,Long> {
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN TRUE ELSE FALSE END FROM Tag t WHERE lower(t.name) = lower(?1)")
    boolean existsByName(String name);
    Optional<Tag> findByName(String name);
}
