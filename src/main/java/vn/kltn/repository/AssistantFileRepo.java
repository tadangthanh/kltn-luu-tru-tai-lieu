package vn.kltn.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.kltn.entity.AssistantFile;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssistantFileRepo extends JpaRepository<AssistantFile, Long> {

    void deleteByName(String name);

    Optional<AssistantFile> findByName(String name);

    @Query("SELECT af FROM AssistantFile af inner join  ChatSession cs on af.chatSession.id = cs.id WHERE af.chatSession.id = :chatSessionId and cs.user.id = :userId")
    List<AssistantFile> findAllByChatSessionId(Long chatSessionId,Long userId);
}
