package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import vn.kltn.common.TransferStatus;
import vn.kltn.dto.response.OwnerShipTransferResponse;
import vn.kltn.entity.Document;
import vn.kltn.entity.Folder;
import vn.kltn.entity.OwnerShipTransfer;
import vn.kltn.entity.User;
import vn.kltn.exception.ConflictResourceException;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.map.OwnerShipTransferMapper;
import vn.kltn.repository.OwnerShipTransferRepo;
import vn.kltn.service.IMailService;
import vn.kltn.service.IOwnerShipTransferService;
import vn.kltn.service.IUserService;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j(topic = "OWNER_SHIP_TRANSFER_SERVICE")
public class OwnerShipTransferServiceImpl implements IOwnerShipTransferService {
    private final DocumentCommonService documentCommonService;
    private final OwnerShipTransferRepo ownerShipTransferRepo;
    private final IUserService userService;
    private final IMailService mailService;
    private final OwnerShipTransferMapper ownerShipTransferMapper;
    private final FolderCommonService folderCommonService;

    @Override
    public OwnerShipTransferResponse createTransferDocumentOwner(Long documentId, Long newOwnerId) {
        validateOwnerShipNotExistDocumentByStatus(documentId, newOwnerId, TransferStatus.PENDING);
        OwnerShipTransfer ownerShipTransfer = createOwnerShipTransferWithDocument(documentId, newOwnerId);
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

    private OwnerShipTransfer createOwnerShipTransferWithDocument(Long documentId, Long newOwnerId) {
        OwnerShipTransfer transfer = new OwnerShipTransfer();
        setDocumentToOwnerShip(transfer, documentId);
        setNewOwner(transfer, newOwnerId);
        transfer.setStatus(TransferStatus.PENDING);
        return ownerShipTransferRepo.save(transfer);
    }

    private OwnerShipTransfer createOwnerShipTransferWithFolder(Long folderId, Long newOwnerId) {
        OwnerShipTransfer transfer = new OwnerShipTransfer();
        setFolderToOwnerShip(transfer, folderId);
        setNewOwner(transfer, newOwnerId);
        transfer.setStatus(TransferStatus.PENDING);
        return ownerShipTransferRepo.save(transfer);
    }

    private void setDocumentToOwnerShip(OwnerShipTransfer transfer, Long documentId) {
        Document document = documentCommonService.getDocumentByIdOrThrow(documentId);
        validateConditionsDocumentTransfer(document);
        transfer.setDocument(document);
    }

    private void setFolderToOwnerShip(OwnerShipTransfer transfer, Long folderId) {
        Folder folder = folderCommonService.getFolderByIdOrThrow(folderId);
        validateConditionsFolderToTransferOwner(folder);
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
    private void validateConditionsDocumentTransfer(Document document) {
        documentCommonService.validateDocumentNotDeleted(document);
        documentCommonService.validateCurrentUserIsOwnerDocument(document);
    }

    private void validateConditionsFolderToTransferOwner(Folder folder) {
        folderCommonService.validateFolderNotDeleted(folder);
        folderCommonService.validateCurrentUserIsOwnerFolder(folder);
    }

    @Override
    public OwnerShipTransferResponse createTransferFolderOwner(Long folderId, Long newOwnerId) {
        validateOwnerShipNotExistFolderByStatus(folderId, newOwnerId, TransferStatus.PENDING);
        OwnerShipTransfer ownerShipTransfer = createOwnerShipTransferWithFolder(folderId, newOwnerId);
        sendEmailTransferOwnerShipFolder(ownerShipTransfer.getNewOwner(), ownerShipTransfer.getFolder());
        return mapToOwnerShipTransferResponse(ownerShipTransfer);
    }

    @Override
    public OwnerShipTransferResponse acceptTransferByDocumentId(Long documentId) {
        OwnerShipTransfer ownerShipTransfer = getTransferByDocumentIdAndCurrentUser(documentId);
        validateTransferStatusIsPending(ownerShipTransfer);
        Document document = ownerShipTransfer.getDocument();
        documentCommonService.validateDocumentNotDeleted(document);
        transferOwnership(ownerShipTransfer);
        return mapToOwnerShipTransferResponse(ownerShipTransfer);
    }

    private void validateTransferStatusIsPending(OwnerShipTransfer transfer) {
        if (transfer.getStatus() == TransferStatus.ACCEPTED) {
            throw new ConflictResourceException("Yêu cầu đã được chấp nhận");
        }
        if (transfer.getStatus() == TransferStatus.DECLINED) {
            throw new ConflictResourceException("Yêu cầu đã được từ chối");
        }
    }

    private void transferOwnership(OwnerShipTransfer transfer) {
        if (transfer.getDocument() != null) {
            transfer.getDocument().setOwner(transfer.getNewOwner());
        }
        if (transfer.getFolder() != null) {
            transfer.getFolder().setOwner(transfer.getNewOwner());
        }
        transfer.setStatus(TransferStatus.ACCEPTED);
    }

    private OwnerShipTransfer getTransferByDocumentIdAndCurrentUser(Long documentId) {
        User currentUser = userService.getUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
        return ownerShipTransferRepo.findByDocumentIdAndNewOwnerId(documentId, currentUser.getId()).orElseThrow(() -> {
            log.warn("Không tìm thấy yêu cầu chuyển quyền sở hữu với tài liệu này: {}", documentId);
            return new ResourceNotFoundException("Không tìm thấy yêu cầu chuyển quyền sở hữu với tài liệu này");
        });
    }

    @Override
    public OwnerShipTransferResponse declineTransferByDocumentId(Long documentId) {
        return null;
    }

    @Override
    public OwnerShipTransferResponse acceptTransferByFolderId(Long folderId) {
        return null;
    }

    @Override
    public OwnerShipTransferResponse declineTransferByFolderId(Long folderId) {
        return null;
    }

    private void validateOwnerShipNotExistDocumentByStatus(Long documentId, Long newOwnerId, TransferStatus status) {
        if (ownerShipTransferRepo.existOwnerShipTransferDocument(documentId, newOwnerId, status)) {
            throw new ConflictResourceException("Đã có yêu cầu chuyển quyền sở hữu với tài liệu này, chờ xử lý");
        }
    }

    private void validateOwnerShipNotExistFolderByStatus(Long folderId, Long newOwnerId, TransferStatus status) {
        if (ownerShipTransferRepo.existOwnerShipTransferFolder(folderId, newOwnerId, status)) {
            throw new ConflictResourceException("Đã có yêu cầu chuyển quyền sở hữu với thư mục này, chờ xử lý");
        }
    }
}
