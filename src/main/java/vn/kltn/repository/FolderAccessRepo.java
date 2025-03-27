package vn.kltn.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.kltn.entity.FolderAccess;

@Repository
public interface FolderAccessRepo extends JpaRepository<FolderAccess, Long> {
    @Modifying
    @Transactional
    @Query("delete from FolderAccess fa where fa.resource.id = :folderId and fa.recipient.id = :recipientId")
    void deleteByFolderIdAndRecipientId(Long folderId, Long recipientId);
}
