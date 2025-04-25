package vn.kltn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.kltn.entity.OwnerShipTransfer;

import java.util.Optional;

@Repository
public interface OwnerShipTransferRepo extends JpaRepository<OwnerShipTransfer, Long> {
    @Query("SELECT o FROM OwnerShipTransfer o WHERE o.document.id = :documentId AND o.newOwner.id = :newOwnerId")
    Optional<OwnerShipTransfer> findByDocumentIdAndNewOwnerId(Long documentId, Long newOwnerId);

    @Query("SELECT o FROM OwnerShipTransfer o WHERE o.folder.id = :folderId AND o.newOwner.id = :newOwnerId")
    Optional<OwnerShipTransfer> findByFolderIdAndNewOwnerId(Long folderId, Long newOwnerId);

    @Query("SELECT o FROM OwnerShipTransfer o WHERE o.document.id = :documentId AND o.oldOwner.id = :oldOwnerId")
    Optional<OwnerShipTransfer> findByDocumentIdAndOldOwnerId(Long documentId, Long oldOwnerId);

    @Query("SELECT o FROM OwnerShipTransfer o WHERE o.folder.id = :folderId AND o.oldOwner.id = :oldOwnerId")
    Optional<OwnerShipTransfer> findByFolderIdAndOldOwnerId(Long folderId, Long oldOwnerId);
}
