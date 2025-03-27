package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import vn.kltn.dto.request.AccessRequest;
import vn.kltn.dto.response.AccessResourceResponse;
import vn.kltn.entity.Document;
import vn.kltn.entity.DocumentAccess;
import vn.kltn.entity.User;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.map.AccessResourceMapper;
import vn.kltn.repository.DocumentAccessRepo;
import vn.kltn.service.IDocumentAccessService;
import vn.kltn.service.IDocumentService;
import vn.kltn.service.IMailService;
import vn.kltn.service.IUserService;

@Service
@Transactional
@Slf4j(topic = "DOCUMENT_ACCESS_SERVICE")
@RequiredArgsConstructor
public class DocumentAccessServiceImpl extends AbstractAccessService<DocumentAccess, AccessResourceResponse> implements IDocumentAccessService {
    private final DocumentAccessRepo documentAccessRepo;
    private final AccessResourceMapper accessResourceMapper;
    private final IDocumentService documentService;
    private final IUserService userService;
    private final IMailService mailService;
    private final ResourceCommonService resourceCommonService;


    private void validateConditionsAccess(Document document) {
        // chưa bị xóa
        resourceCommonService.validateResourceNotDeleted(document);
        // là chủ sở hữu
        resourceCommonService.validateCurrentUserIsOwnerResource(document);
    }

    @Override
    protected Page<DocumentAccess> getPageAccessByResource(Pageable pageable) {
        return documentAccessRepo.findAll(pageable);
    }

    @Override
    protected Page<DocumentAccess> getPageAccessByResourceBySpec(Specification<DocumentAccess> spec, Pageable pageable) {
        return documentAccessRepo.findAll(spec, pageable);
    }

    @Override
    protected AccessResourceResponse mapToR(DocumentAccess access) {
        return accessResourceMapper.toAccessResourceResponse(access);
    }

    @Override
    protected void sendEmailInviteAccess(DocumentAccess access, AccessRequest accessRequest) {
        mailService.sendEmailInviteDocumentAccess(accessRequest.getRecipientEmail(), access, accessRequest.getMessage());
    }

    @Override
    protected DocumentAccess createEmptyAccess() {
        return new DocumentAccess();
    }

    @Override
    protected void setResource(DocumentAccess access, Long resourceId) {
        Document document = documentService.getDocumentByIdOrThrow(resourceId);
        validateConditionsAccess(document);
        access.setResource(document);
    }

    @Override
    protected DocumentAccess findAccessById(Long accessId) {
        return documentAccessRepo.findById(accessId).orElseThrow(() -> {
            log.warn("Document access not found by id: {}", accessId);
            return new ResourceNotFoundException("Document access not found");
        });
    }

    @Override
    protected DocumentAccess saveAccess(DocumentAccess access) {
        return documentAccessRepo.save(access);
    }

    @Override
    protected void deleteAccessEntity(DocumentAccess access) {
        documentAccessRepo.delete(access);
    }

    @Override
    protected User getUserByEmail(String email) {
        return userService.getUserByEmail(email);
    }
}
