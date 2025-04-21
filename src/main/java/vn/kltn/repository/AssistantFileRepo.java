package vn.kltn.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.kltn.entity.AssistantFile;

import java.util.Optional;

@Repository
public interface AssistantFileRepo extends JpaRepository<AssistantFile, Long> {
    @Query("SELECT af FROM AssistantFile af WHERE af.user.id = :id")
    Page<AssistantFile> findAllByCurrentUser(Long id, Pageable pageable);

    void deleteByName(String name);

    Optional<AssistantFile> findByName(String name);
}
