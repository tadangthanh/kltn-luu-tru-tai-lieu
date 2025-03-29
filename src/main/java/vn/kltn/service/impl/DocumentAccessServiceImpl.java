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
import vn.kltn.dto.response.DocumentResponse;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.entity.*;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.map.AccessResourceMapper;
import vn.kltn.repository.DocumentAccessRepo;
import vn.kltn.repository.specification.EntitySpecificationsBuilder;
import vn.kltn.repository.util.PaginationUtils;
import vn.kltn.service.*;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional
@Slf4j(topic = "DOCUMENT_ACCESS_SERVICE")
@RequiredArgsConstructor
public class DocumentAccessServiceImpl extends AbstractAccessService<DocumentAccess, AccessResourceResponse> implements IDocumentAccessService {
    private final DocumentAccessRepo documentAccessRepo;
    private final AccessResourceMapper accessResourceMapper;
    private final IUserService userService;
    private final IMailService mailService;
    private final ResourceCommonService resourceCommonService;
    private final IAuthenticationService authenticationService;
    private final IFolderAccessService folderAccessService;


    private void validateConditionsToAccess(Document document) {
        // chưa bị xóa
        resourceCommonService.validateResourceNotDeleted(document);
        // là chủ sở hữu
        resourceCommonService.validateCurrentUserIsOwnerResource(document);
    }


    @Override
    protected DocumentAccess getAccessByResourceAndRecipient(Long resourceId, Long recipientId) {
        return documentAccessRepo.findByResourceIdAndRecipientId(resourceId, recipientId).orElseThrow(() -> {
            log.warn("Document access not found by resource id: {} and recipient id: {}", resourceId, recipientId);
            return new ResourceNotFoundException("Bạn không có quyền thực hiện hành động này!");
        });
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
        Document document = resourceCommonService.getDocumentByIdOrThrow(resourceId);
        validateConditionsToAccess(document);
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

    @Override
    protected User getCurrentUser() {
        return authenticationService.getCurrentUser();
    }

    @Override
    protected void deleteAccessByResourceIdAndRecipient(Long resourceId, Long recipientId) {
        documentAccessRepo.deleteByResourceAndRecipientId(resourceId, recipientId);
    }

    @Override
    public void inheritAccess(Document document) {
        Folder folder = document.getParent();
        if (folder != null) {
            Set<FolderAccess> folderAccesses = folderAccessService.getAllByResourceId(folder.getId());
            folderAccesses.forEach(folderAccess -> {
                DocumentAccess documentAccess = createEmptyAccess();
                documentAccess.setRecipient(folderAccess.getRecipient());
                documentAccess.setPermission(folderAccess.getPermission());
                documentAccess.setResource(document);
                documentAccessRepo.save(documentAccess);
            });
        }
    }

    @Override
    public PageResponse<List<DocumentResponse>> getPageDocumentSharedByCurrentUser(Pageable pageable, String[] documents) {
        if (documents != null && documents.length > 0) {
            EntitySpecificationsBuilder<Document> builder = new EntitySpecificationsBuilder<>();
            Pattern pattern = Pattern.compile("([a-zA-Z0-9_.]+?)([<:>~!])(.*)(\\p{Punct}?)(\\p{Punct}?)");
            for (String s : documents) {
                Matcher matcher = pattern.matcher(s);
                if (matcher.find()) {
                    builder.with(matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4), matcher.group(5));
                }
            }
            Specification<Document> spec = builder.build();
            Page<Document> pageResource = resourceCommonService.pagePageDocumentBySpec(spec, pageable);
            return PaginationUtils.convertToPageResponse(pageResource, pageable, resourceCommonService::mapToDocumentResponse);
        }
        return PaginationUtils.convertToPageResponse(resourceCommonService.getPageDocument(pageable), pageable, resourceCommonService::mapToDocumentResponse);
    }

    @Override
    public Set<DocumentAccess> getAllByResourceId(Long resourceId) {
        return documentAccessRepo.findAllByResourceId(resourceId);
    }

    @Override
    public void deleteAccessByResource(Long resourceId) {
        documentAccessRepo.deleteAllByResourceId(resourceId);
    }
}
