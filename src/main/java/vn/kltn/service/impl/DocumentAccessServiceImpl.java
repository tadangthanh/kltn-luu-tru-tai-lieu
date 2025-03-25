package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.kltn.dto.request.AccessRequest;
import vn.kltn.dto.response.DocumentAccessResponse;
import vn.kltn.entity.Document;
import vn.kltn.entity.DocumentAccess;
import vn.kltn.entity.User;
import vn.kltn.map.DocumentAccessMapper;
import vn.kltn.repository.DocumentAccessRepo;
import vn.kltn.service.IDocumentAccessService;
import vn.kltn.service.IDocumentService;
import vn.kltn.service.IMailService;
import vn.kltn.service.IUserService;

@Service
@Transactional
@Slf4j(topic = "DOCUMENT_ACCESS_SERVICE")
@RequiredArgsConstructor
public class DocumentAccessServiceImpl implements IDocumentAccessService {
    private final DocumentAccessRepo documentAccessRepo;
    private final DocumentAccessMapper documentAccessMapper;
    private final IDocumentService documentService;
    private final IUserService userService;
    private final IMailService mailService;

    @Override
    public DocumentAccessResponse createDocumentAccess(Long documentId, AccessRequest accessRequest) {
        Document document = documentService.getDocumentByIdOrThrow(documentId);
        validateConditionsAccess(document);
        DocumentAccess documentAccess = saveDocumentToAccess(document, accessRequest);
        sendEmailInviteDocumentAccess(documentAccess, accessRequest);
        return mapToDocumentAccessResponse(documentAccess);
    }

    private void sendEmailInviteDocumentAccess(DocumentAccess documentAccess, AccessRequest accessRequest) {
        mailService.sendEmailInviteDocumentAccess(accessRequest.getRecipientEmail(), documentAccess, accessRequest.getMessage());
    }

    private void validateConditionsAccess(Document document) {
        // chưa bị xóa
        documentService.validateDocumentNotDeleted(document);
        // là chủ sở hữu
        documentService.validateCurrentUserIsOwnerDocument(document);
    }

    private DocumentAccessResponse mapToDocumentAccessResponse(DocumentAccess documentAccess) {
        return documentAccessMapper.toDocumentAccessResponse(documentAccess);
    }

    private DocumentAccess saveDocumentToAccess(Document document, AccessRequest accessRequest) {
        DocumentAccess documentAccess = new DocumentAccess();
        documentAccess.setDocument(document);
        documentAccess.setPermission(accessRequest.getPermission());
        User recipient = userService.getUserByEmail(accessRequest.getRecipientEmail());
        documentAccess.setRecipient(recipient);
        return documentAccessRepo.save(documentAccess);

    }

    @Override
    public void deleteDocumentAccess(Long documentId, Long recipientId) {
        Document document = documentService.getDocumentByIdOrThrow(documentId);
        validateConditionsAccess(document);
        documentAccessRepo.deleteByDocumentIdAndRecipientId(documentId, recipientId);
    }
}
