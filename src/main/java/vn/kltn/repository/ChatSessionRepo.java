package vn.kltn.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.kltn.entity.ChatSession;

@Repository
public interface ChatSessionRepo extends JpaRepository<ChatSession, Long> {
    @Query("SELECT cs FROM ChatSession cs WHERE cs.user.id = :userId")
    Page<ChatSession> findAllByUser(Long userId, Pageable pageable);
}
