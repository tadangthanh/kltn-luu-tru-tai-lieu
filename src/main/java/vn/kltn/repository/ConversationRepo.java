package vn.kltn.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.kltn.entity.Conversation;

import java.util.List;

@Repository
public interface ConversationRepo extends JpaRepository<Conversation, Long> {

    @Query("SELECT c FROM Conversation c WHERE c.assistantFile.id = :assistantFileId")
    Page<Conversation> findAllByAssistantFile(Long assistantFileId, Pageable pageable);

    @Query("SELECT c FROM Conversation c WHERE c.assistantFile.id = :assistantFileId order by c.id asc")
    List<Conversation> findAllByAssistantFileId(Long assistantFileId);
}
