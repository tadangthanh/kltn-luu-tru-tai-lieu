package vn.kltn.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.kltn.entity.DocumentAccess;

@Repository
public interface DocumentAccessRepo extends JpaRepository<DocumentAccess, Long> {

    @Modifying
    @Transactional
    @Query("delete from DocumentAccess da where da.resource.id = :documentId and da.recipient.id = :recipientId")
    void deleteByDocumentIdAndRecipientId(Long documentId, Long recipientId);
}
