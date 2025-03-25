package vn.kltn.service;

import vn.kltn.dto.response.OwnerShipTransferResponse;

public interface IOwnerShipTransferService {
    OwnerShipTransferResponse createTransferDocumentOwner(Long documentId, Long newOwnerId);

    OwnerShipTransferResponse createTransferFolderOwner(Long folderId, Long newOwnerId);

    OwnerShipTransferResponse acceptTransferByDocumentId(Long documentId);

    OwnerShipTransferResponse declineTransferByDocumentId(Long documentId);

    OwnerShipTransferResponse acceptTransferByFolderId(Long folderId);

    OwnerShipTransferResponse declineTransferByFolderId(Long folderId);
}
