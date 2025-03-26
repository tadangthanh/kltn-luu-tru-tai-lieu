package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    // Tạo yêu cầu chuyển quyền sở hữu
    @Override
    public OwnerShipTransferResponse createTransferDocumentOwner(Long documentId, Long newOwnerId) {
        return createTransfer(documentId, newOwnerId, true);
    }

    @Override
    public OwnerShipTransferResponse createTransferFolderOwner(Long folderId, Long newOwnerId) {
        return createTransfer(folderId, newOwnerId, false);
    }

    // Chấp nhận yêu cầu chuyển quyền
    @Override
    public OwnerShipTransferResponse acceptTransferByDocumentId(Long documentId) {
        return acceptTransfer(documentId, true);
    }

    @Override
    public OwnerShipTransferResponse acceptTransferByFolderId(Long folderId) {
        return acceptTransfer(folderId, false);
    }

    // Từ chối yêu cầu chuyển quyền
    @Override
    public OwnerShipTransferResponse declineTransferByDocumentId(Long documentId) {
        return declineTransfer(documentId, true);
    }

    @Override
    public OwnerShipTransferResponse declineTransferByFolderId(Long folderId) {
        return declineTransfer(folderId, false);
    }

    // Phương thức trợ giúp: Tạo yêu cầu chuyển quyền chung
    private OwnerShipTransferResponse createTransfer(Long resourceId, Long newOwnerId, boolean isDocument) {
        User oldOwner = authenticationService.getCurrentUser();
        OwnerShipTransfer ownerShipTransfer = isDocument
                ? getTransferByDocumentAndOldOwner(resourceId, oldOwner.getId())
                : getTransferByFolderAndOldOwner(resourceId, oldOwner.getId());
        Resource resource;

        if (ownerShipTransfer != null) {
            resource = isDocument ? ownerShipTransfer.getDocument() : ownerShipTransfer.getFolder();
            validateConditionsResourceTransfer(resource);
            validateTransferStatusNotPending(ownerShipTransfer);
            validateTransferStatusNotAccepted(ownerShipTransfer);
            ownerShipTransfer.setStatus(TransferStatus.PENDING);
        } else {
            resource = isDocument
                    ? resourceCommonService.getDocumentByIdOrThrow(resourceId)
                    : resourceCommonService.getFolderByIdOrThrow(resourceId);
            validateConditionsResourceTransfer(resource);
            ownerShipTransfer = new OwnerShipTransfer();
            if (isDocument) {
                ownerShipTransfer.setDocument((Document) resource);
            } else {
                ownerShipTransfer.setFolder((Folder) resource);
            }
            ownerShipTransfer.setOldOwner(oldOwner);
            ownerShipTransfer.setNewOwner(userService.getUserById(newOwnerId));
            ownerShipTransfer.setStatus(TransferStatus.PENDING);
            ownerShipTransfer = ownerShipTransferRepo.save(ownerShipTransfer);
        }
        sendEmailTransferOwnerShip(resource, ownerShipTransfer.getNewOwner());
        return mapToOwnerShipTransferResponse(ownerShipTransfer);
    }

    // Phương thức trợ giúp: Chấp nhận yêu cầu chuyển quyền
    private OwnerShipTransferResponse acceptTransfer(Long resourceId, boolean isDocument) {
        User newOwner = authenticationService.getCurrentUser();
        OwnerShipTransfer ownerShipTransfer = isDocument
                ? getTransferByDocumentIdAndNewOwner(resourceId, newOwner.getId())
                : getTransferByFolderIdAndNewOwner(resourceId, newOwner.getId());
        validateTransferStatusIsPending(ownerShipTransfer);
        Resource resource = isDocument ? ownerShipTransfer.getDocument() : ownerShipTransfer.getFolder();
        resourceCommonService.validateResourceNotDeleted(resource);
        transferOwnership(ownerShipTransfer);
        return mapToOwnerShipTransferResponse(ownerShipTransfer);
    }

    // Phương thức trợ giúp: Từ chối yêu cầu chuyển quyền
    private OwnerShipTransferResponse declineTransfer(Long resourceId, boolean isDocument) {
        User newOwner = authenticationService.getCurrentUser();
        OwnerShipTransfer ownerShipTransfer = isDocument
                ? getTransferByDocumentIdAndNewOwner(resourceId, newOwner.getId())
                : getTransferByFolderIdAndNewOwner(resourceId, newOwner.getId());
        validateTransferStatusIsPending(ownerShipTransfer);
        Resource resource = isDocument ? ownerShipTransfer.getDocument() : ownerShipTransfer.getFolder();
        resourceCommonService.validateResourceNotDeleted(resource);
        ownerShipTransfer.setStatus(TransferStatus.DECLINED);
        return mapToOwnerShipTransferResponse(ownerShipTransfer);
    }

    // Phương thức trợ giúp: Gửi email thông báo
    private void sendEmailTransferOwnerShip(Resource resource, User newOwner) {
        if (resource instanceof Document) {
            mailService.sendEmailTransferOwnershipDocument(newOwner.getEmail(), (Document) resource);
        } else if (resource instanceof Folder) {
            mailService.sendEmailTransferOwnershipFolder(newOwner.getEmail(), (Folder) resource);
        }
    }

    // Chuyển quyền sở hữu
    private void transferOwnership(OwnerShipTransfer transfer) {
        Resource resource = transfer.getDocument() != null ? transfer.getDocument() : transfer.getFolder();
        resource.setOwner(transfer.getNewOwner());
        transfer.setStatus(TransferStatus.ACCEPTED);
    }

    // Lấy thông tin yêu cầu chuyển quyền
    private OwnerShipTransfer getTransferByDocumentAndOldOwner(Long documentId, Long oldOwnerId) {
        return ownerShipTransferRepo.findByDocumentIdAndOldOwnerId(documentId, oldOwnerId).orElse(null);
    }

    private OwnerShipTransfer getTransferByFolderAndOldOwner(Long folderId, Long oldOwnerId) {
        return ownerShipTransferRepo.findByFolderIdAndOldOwnerId(folderId, oldOwnerId).orElse(null);
    }

    private OwnerShipTransfer getTransferByDocumentIdAndNewOwner(Long documentId, Long newOwnerId) {
        return ownerShipTransferRepo.findByDocumentIdAndNewOwnerId(documentId, newOwnerId).orElseThrow(() -> {
            log.warn("Không tìm thấy yêu cầu chuyển quyền sở hữu");
            return new ConflictResourceException("Không tìm thấy yêu cầu chuyển quyền sở hữu");
        });
    }

    private OwnerShipTransfer getTransferByFolderIdAndNewOwner(Long folderId, Long newOwnerId) {
        return ownerShipTransferRepo.findByFolderIdAndNewOwnerId(folderId, newOwnerId).orElseThrow(() -> {
            log.warn("Không tìm thấy yêu cầu chuyển quyền sở hữu");
            return new ConflictResourceException("Không tìm thấy yêu cầu chuyển quyền sở hữu");
        });
    }

    // Chuyển đổi sang DTO response
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

    // Các phương thức kiểm tra điều kiện
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

    private void validateTransferStatusIsPending(OwnerShipTransfer ownerShipTransfer) {
        if (ownerShipTransfer.getStatus() == TransferStatus.ACCEPTED) {
            throw new ConflictResourceException("Yêu cầu đã được chấp nhận");
        }
        if (ownerShipTransfer.getStatus() == TransferStatus.DECLINED) {
            throw new ConflictResourceException("Yêu cầu đã được từ chối");
        }
    }

    private <T extends Resource> void validateConditionsResourceTransfer(T resource) {
        resourceCommonService.validateResourceNotDeleted(resource);
        resourceCommonService.validateCurrentUserIsOwnerResource(resource);
    }
}