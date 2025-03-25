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
        DocumentAccess documentAccess = mapToDocumentAccess(documentId, accessRequest);
        documentAccess = documentAccessRepo.save(documentAccess);
        mailService.sendEmailInviteDocumentAccess(accessRequest.getRecipientEmail(), documentAccess,accessRequest.getMessage());
        return mapToDocumentAccessResponse(documentAccess);
    }

    private DocumentAccessResponse mapToDocumentAccessResponse(DocumentAccess documentAccess) {
        return documentAccessMapper.toDocumentAccessResponse(documentAccess);
    }

    private DocumentAccess mapToDocumentAccess(Long documentId, AccessRequest accessRequest) {
        DocumentAccess documentAccess = new DocumentAccess();
        Document document = documentService.getDocumentByIdOrThrow(documentId);
        documentAccess.setDocument(document);
        documentAccess.setPermission(accessRequest.getPermission());
        User recipient = userService.getUserByEmail(accessRequest.getRecipientEmail());
        documentAccess.setRecipient(recipient);
        return documentAccess;

    }

    @Override
    public void deleteDocumentAccess(Long documentId, Long recipientId) {

    }
}
