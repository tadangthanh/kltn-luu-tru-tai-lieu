package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import vn.kltn.common.TransferStatus;
import vn.kltn.dto.response.OwnerShipTransferResponse;
import vn.kltn.entity.*;
import vn.kltn.exception.ConflictResourceException;
import vn.kltn.map.OwnerShipTransferMapper;
import vn.kltn.repository.OwnerShipTransferRepo;
import vn.kltn.service.IAuthenticationService;
import vn.kltn.service.IMailService;
import vn.kltn.service.IOwnerShipTransferService;
import vn.kltn.service.IUserService;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j(topic = "OWNER_SHIP_TRANSFER_SERVICE")
public class OwnerShipTransferServiceImpl implements IOwnerShipTransferService {
    private final OwnerShipTransferRepo ownerShipTransferRepo;
    private final IUserService userService;
    private final IMailService mailService;
    private final OwnerShipTransferMapper ownerShipTransferMapper;
    private final ResourceCommonService resourceCommonService;
    private final IAuthenticationService authenticationService;

    @Override
    public OwnerShipTransferResponse createTransferDocumentOwner(Long documentId, Long newOwnerId) {
        User oldOwner = authenticationService.getCurrentUser();
        OwnerShipTransfer ownerShipTransfer = getTransferByDocumentAndOldOwner(documentId, oldOwner.getId());
        if (ownerShipTransfer != null) {
            validateTransferStatusNotPending(ownerShipTransfer);
            validateTransferStatusNotAccepted(ownerShipTransfer);
            ownerShipTransfer.setStatus(TransferStatus.PENDING);
            sendEmailTransferOwnerShipDocument(ownerShipTransfer.getNewOwner(), ownerShipTransfer.getDocument());
            return mapToOwnerShipTransferResponse(ownerShipTransfer);
        }
        ownerShipTransfer = createOwnerShipTransferDocument(documentId, newOwnerId);
        sendEmailTransferOwnerShipDocument(ownerShipTransfer.getNewOwner(), ownerShipTransfer.getDocument());
        return mapToOwnerShipTransferResponse(ownerShipTransfer);
    }

    private OwnerShipTransferResponse mapToOwnerShipTransferResponse(OwnerShipTransfer ownerShipTransfer) {
        OwnerShipTransferResponse response = ownerShipTransferMapper.toOwnerShipTransferResponse(ownerShipTransfer);
        if (ownerShipTransfer.getDocument() != null) {
            response.setDocumentId(ownerShipTransfer.getDocument().getId());
            response.setDocumentName(ownerShipTransfer.getDocument().getName());
        }
        if (ownerShipTransfer.getFolder() != null) {
            response.setFolderId(ownerShipTransfer.getFolder().getId());
            response.setFolderName(ownerShipTransfer.getFolder().getName());
        }
        return response;
    }

    private void validateTransferStatusNotPending(OwnerShipTransfer ownerShipTransfer) {
        if (ownerShipTransfer.getStatus() == TransferStatus.PENDING) {
            throw new ConflictResourceException("Đang chờ xác nhận từ chủ sở hữu mới");
        }
    }

    private void validateTransferStatusNotAccepted(OwnerShipTransfer ownerShipTransfer) {
        if (ownerShipTransfer.getStatus() == TransferStatus.ACCEPTED) {
            throw new ConflictResourceException("Đã chấp nhận yêu cầu");
        }
    }

    private OwnerShipTransfer createOwnerShipTransferDocument(Long documentId, Long newOwnerId) {
        OwnerShipTransfer transfer = new OwnerShipTransfer();
        setDocumentToOwnerShip(transfer, documentId);
        setNewOwner(transfer, newOwnerId);
        transfer.setStatus(TransferStatus.PENDING);
        return ownerShipTransferRepo.save(transfer);
    }

    private OwnerShipTransfer createOwnerShipTransferFolder(Long folderId, Long newOwnerId) {
        OwnerShipTransfer transfer = new OwnerShipTransfer();
        setFolderToOwnerShip(transfer, folderId);
        setNewOwner(transfer, newOwnerId);
        transfer.setStatus(TransferStatus.PENDING);
        return ownerShipTransferRepo.save(transfer);
    }

    // gán document cho owner ship transfer
    private void setDocumentToOwnerShip(OwnerShipTransfer transfer, Long documentId) {
        Document document = resourceCommonService.getDocumentByIdOrThrow(documentId);
        validateConditionsResourceTransfer(document);
        transfer.setDocument(document);
    }

    // gán folder cho owner ship transfer
    private void setFolderToOwnerShip(OwnerShipTransfer transfer, Long folderId) {
        Folder folder = resourceCommonService.getFolderByIdOrThrow(folderId);
        validateConditionsResourceTransfer(folder);
        transfer.setFolder(folder);
    }

    private void setNewOwner(OwnerShipTransfer transfer, Long newOwnerId) {
        User oldOwner = userService.getUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
        transfer.setOldOwner(oldOwner);
        User newOwner = userService.getUserById(newOwnerId);
        transfer.setNewOwner(newOwner);
    }

    private void sendEmailTransferOwnerShipDocument(User newOwner, Document document) {
        mailService.sendEmailTransferOwnershipDocument(newOwner.getEmail(), document);
    }

    private void sendEmailTransferOwnerShipFolder(User newOwner, Folder folder) {
        mailService.sendEmailTransferOwnershipFolder(newOwner.getEmail(), folder);
    }

    // document chua bi xoa
    // nguoi thuc hien yeu cau phai la owner cua document
    private <T extends Resource> void validateConditionsResourceTransfer(T resource) {
        resourceCommonService.validateResourceNotDeleted(resource);
        resourceCommonService.validateCurrentUserIsOwnerResource(resource);
    }

    @Override
    public OwnerShipTransferResponse createTransferFolderOwner(Long folderId, Long newOwnerId) {
        User oldOwner = authenticationService.getCurrentUser();
        OwnerShipTransfer ownerShipTransfer = getTransferByFolderAndOldOwner(folderId, oldOwner.getId());
        if (ownerShipTransfer != null) {
            validateTransferStatusNotAccepted(ownerShipTransfer);
            validateTransferStatusNotPending(ownerShipTransfer);
            ownerShipTransfer.setStatus(TransferStatus.PENDING);
            sendEmailTransferOwnerShipFolder(ownerShipTransfer.getNewOwner(), ownerShipTransfer.getFolder());
            return mapToOwnerShipTransferResponse(ownerShipTransfer);
        }
        ownerShipTransfer = createOwnerShipTransferFolder(folderId, newOwnerId);
        sendEmailTransferOwnerShipFolder(ownerShipTransfer.getNewOwner(), ownerShipTransfer.getFolder());
        return mapToOwnerShipTransferResponse(ownerShipTransfer);
    }

    @Override
    public OwnerShipTransferResponse acceptTransferByDocumentId(Long documentId) {
        User newOwner = authenticationService.getCurrentUser();
        OwnerShipTransfer ownerShipTransfer = getTransferByDocumentIdAndNewOwner(documentId, newOwner.getId());
        validateTransferStatusIsPending(ownerShipTransfer);
        Document document = ownerShipTransfer.getDocument();
        resourceCommonService.validateResourceNotDeleted(document);
        transferOwnership(ownerShipTransfer);
        return mapToOwnerShipTransferResponse(ownerShipTransfer);
    }


    // chuyen quyen so huu
    private void transferOwnership(OwnerShipTransfer transfer) {
        if (transfer.getDocument() != null) {
            transfer.getDocument().setOwner(transfer.getNewOwner());
        }
        if (transfer.getFolder() != null) {
            transfer.getFolder().setOwner(transfer.getNewOwner());
        }
        transfer.setStatus(TransferStatus.ACCEPTED);
    }

    private OwnerShipTransfer getTransferByDocumentIdAndNewOwner(Long documentId, Long newOwnerId) {
        return ownerShipTransferRepo.findByDocumentIdAndNewOwnerId(documentId, newOwnerId).orElseThrow(() -> {
            log.warn("không tìm thấy yêu cầu chuyển quyền sở hữu");
            return new ConflictResourceException("Không tìm thấy yêu cầu chuyển quyền sở hữu");
        });
    }

    private OwnerShipTransfer getTransferByDocumentAndOldOwner(Long documentId, Long oldOwnerId) {
        return ownerShipTransferRepo.findByDocumentIdAndOldOwnerId(documentId, oldOwnerId).orElse(null);
    }

    private OwnerShipTransfer getTransferByFolderAndOldOwner(Long folderId, Long oldOwnerId) {
        return ownerShipTransferRepo.findByFolderIdAndOldOwnerId(folderId, oldOwnerId).orElse(null);
    }

    private OwnerShipTransfer getTransferByFolderIdAndNewOwner(Long folderId, Long newOwnerId) {
        return ownerShipTransferRepo.findByFolderIdAndNewOwnerId(folderId, newOwnerId).orElseThrow(() -> {
            log.warn("không tìm thấy yêu cầu chuyển quyền sở hữu");
            return new ConflictResourceException("Không tìm thấy yêu cầu chuyển quyền sở hữu");
        });
    }

    @Override
    public OwnerShipTransferResponse declineTransferByDocumentId(Long documentId) {
        User newOwner = authenticationService.getCurrentUser();
        OwnerShipTransfer ownerShipTransfer = getTransferByDocumentIdAndNewOwner(documentId, newOwner.getId());
        validateTransferStatusIsPending(ownerShipTransfer);
        Document document = ownerShipTransfer.getDocument();
        resourceCommonService.validateResourceNotDeleted(document);
        ownerShipTransfer.setStatus(TransferStatus.DECLINED);
        return mapToOwnerShipTransferResponse(ownerShipTransfer);
    }

    @Override
    public OwnerShipTransferResponse acceptTransferByFolderId(Long folderId) {
        User newOwner = authenticationService.getCurrentUser();
        OwnerShipTransfer ownerShipTransfer = getTransferByFolderIdAndNewOwner(folderId, newOwner.getId());
        validateTransferStatusIsPending(ownerShipTransfer);
        Folder folder = ownerShipTransfer.getFolder();
        resourceCommonService.validateResourceNotDeleted(folder);
        transferOwnership(ownerShipTransfer);
        return mapToOwnerShipTransferResponse(ownerShipTransfer);
    }

    @Override
    public OwnerShipTransferResponse declineTransferByFolderId(Long folderId) {
        User newOwner = authenticationService.getCurrentUser();
        OwnerShipTransfer ownerShipTransfer = getTransferByFolderIdAndNewOwner(folderId, newOwner.getId());
        validateTransferStatusIsPending(ownerShipTransfer);
        Folder folder = ownerShipTransfer.getFolder();
        resourceCommonService.validateResourceNotDeleted(folder);
        ownerShipTransfer.setStatus(TransferStatus.DECLINED);
        return mapToOwnerShipTransferResponse(ownerShipTransfer);
    }

    private void validateTransferStatusIsPending(OwnerShipTransfer ownerShipTransfer) {
        if (ownerShipTransfer.getStatus().equals(TransferStatus.ACCEPTED)) {
            throw new ConflictResourceException("Yêu cầu đã được chấp nhận");
        }
        if (ownerShipTransfer.getStatus().equals(TransferStatus.DECLINED)) {
            throw new ConflictResourceException("Yêu cầu đã được từ chối");
        }
    }

}
