package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.kltn.entity.Document;
import vn.kltn.entity.User;
import vn.kltn.exception.InvalidDataException;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.repository.DocumentRepo;
import vn.kltn.service.IAuthenticationService;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j(topic = "DOCUMENT_COMMON_SERVICE")
public class DocumentCommonService {
    private final DocumentRepo documentRepo;
    private final IAuthenticationService authenticationService;

    public Document getDocumentByIdOrThrow(Long documentId) {
        return documentRepo.findById(documentId).orElseThrow(() -> {
            log.warn("Document with id {} not found", documentId);
            return new ResourceNotFoundException("Không tìm thấy document");
        });
    }

    public void validateDocumentNotDeleted(Document document) {
        if (document.getDeletedAt() != null) {
            throw new InvalidDataException("Document đã bị xóa");
        }
    }

    public void validateDocumentDeleted(Document document) {
        if (document.getDeletedAt() == null) {
            throw new InvalidDataException("Document chưa bị xóa");
        }
    }

    public void validateCurrentUserIsOwnerDocument(Document document) {
        log.info("validate current user is owner document");
        User currentUser = authenticationService.getCurrentUser();
        if (!document.getOwner().getId().equals(currentUser.getId())) {
            log.warn("Current user is not owner folder: {}", document.getId());
            throw new InvalidDataException("Bạn không phải chủ sở hữu");
        }
    }

}
