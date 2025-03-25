package vn.kltn.service;

import vn.kltn.dto.response.OwnerShipTransferResponse;
import vn.kltn.entity.OwnerShipTransfer;
import vn.kltn.repository.OwnerShipTransferRepo;

public interface IOwnerShipTransferService {
    OwnerShipTransferResponse createTransferDocumentOwner(Long documentId, Long newOwnerId);

    OwnerShipTransferResponse createTransferFolderOwner(Long folderId, Long newOwnerId);
}
