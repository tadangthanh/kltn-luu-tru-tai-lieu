package vn.kltn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.kltn.common.TransferStatus;
import vn.kltn.entity.OwnerShipTransfer;

import java.util.Optional;

@Repository
public interface OwnerShipTransferRepo extends JpaRepository<OwnerShipTransfer, Long> {
    @Query("SELECT o FROM OwnerShipTransfer o WHERE o.document.id = :documentId AND o.newOwner.id = :newOwnerId AND o.status = :status")
    Optional<OwnerShipTransfer> findByDocumentIdAndNewOwnerIdAndStatus(Long documentId, Long newOwnerId, TransferStatus status);

    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN TRUE ELSE FALSE END FROM OwnerShipTransfer o WHERE o.document.id = :documentId AND o.newOwner.id = :newOwnerId AND o.status = :status")
    boolean existOwnerShipTransferDocument(Long documentId, Long newOwnerId, TransferStatus status);

    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN TRUE ELSE FALSE END FROM OwnerShipTransfer o WHERE o.folder.id = :folderId AND o.newOwner.id = :newOwnerId AND o.status = :status")
    boolean existOwnerShipTransferFolder(Long folderId, Long newOwnerId, TransferStatus status);
}
