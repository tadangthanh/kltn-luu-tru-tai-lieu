package vn.kltn.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.kltn.entity.DocumentAccess;

import java.util.Optional;
import java.util.Set;

@Repository
public interface DocumentAccessRepo extends JpaRepository<DocumentAccess, Long>, JpaSpecificationExecutor<DocumentAccess> {

    @Query("SELECT da FROM DocumentAccess da WHERE da.resource.id = :resourceId")
    Set<DocumentAccess> findAllByResourceId(Long resourceId);

    @Modifying
    @Transactional
    @Query("DELETE FROM DocumentAccess da WHERE da.resource.id = :resourceId")
    void deleteAllByResourceId(Long resourceId);

    @Query("SELECT da FROM DocumentAccess da WHERE da.resource.id = :resourceId AND da.recipient.id = :recipientId")
    Optional<DocumentAccess> findByResourceIdAndRecipientId(Long resourceId, Long recipientId);

    @Modifying
    @Transactional
    @Query("DELETE FROM DocumentAccess da WHERE da.resource.id = :resourceId AND da.recipient.id = :recipientId")
    void deleteByResourceAndRecipientId(Long resourceId, Long recipientId);
}
