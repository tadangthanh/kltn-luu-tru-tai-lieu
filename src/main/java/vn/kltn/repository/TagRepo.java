package vn.kltn.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import vn.kltn.entity.Tag;

import java.util.Optional;
import java.util.Set;

public interface TagRepo extends JpaRepository<Tag, Long> {
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN TRUE ELSE FALSE END FROM Tag t WHERE lower(t.name) = lower(?1)")
    boolean existsByName(String name);

    Optional<Tag> findByName(String name);

    @Query("select t from Tag t join DocumentHasTag dht on t.id=dht.tag.id where dht.document.id=?1")
    Set<Tag> getTagsByDocumentId(Long documentId);
}
