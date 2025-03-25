package vn.kltn.service;

import vn.kltn.dto.response.OwnerShipTransferResponse;

public interface IOwnerShipTransferService {
    OwnerShipTransferResponse createTransferDocumentOwner(Long documentId, Long newOwnerId);

    OwnerShipTransferResponse createTransferFolderOwner(Long folderId, Long newOwnerId);
}
