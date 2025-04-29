package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import vn.kltn.dto.response.DocumentIndexResponse;
import vn.kltn.dto.response.DocumentResponse;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.entity.Document;
import vn.kltn.entity.User;
import vn.kltn.repository.DocumentRepo;
import vn.kltn.repository.specification.EntitySpecificationsBuilder;
import vn.kltn.repository.specification.SpecificationUtil;
import vn.kltn.repository.util.PaginationUtils;
import vn.kltn.service.*;

import java.util.List;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j(topic = "DOCUMENT_SEARCH_SERVICE")
public class DocumentSearchServiceImpl implements IDocumentSearchService {
    private final DocumentRepo documentRepo;
    private final IDocumentMapperService documentMapperService;
    private final IAuthenticationService authenticationService;
    private final IDocumentIndexService documentIndexService;
    private final IPermissionService permissionService;

    @Override
    public PageResponse<List<DocumentResponse>> searchByCurrentUser(Pageable pageable, String[] documents) {
        log.info("search document by current user");
        if (documents != null && documents.length > 0) {
            EntitySpecificationsBuilder<Document> builder = new EntitySpecificationsBuilder<>();
            Specification<Document> spec = SpecificationUtil.buildSpecificationFromFilters(documents, builder);
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.isNull(root.get("deletedAt")));
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("createdBy"), email));
            Page<Document> docPage = documentRepo.findAll(spec, pageable);
            return PaginationUtils.convertToPageResponse(docPage, pageable, documentMapperService::mapToDocumentResponse);
        }
        return PaginationUtils.convertToPageResponse(documentRepo.findAll(pageable), pageable, documentMapperService::mapToDocumentResponse);
    }

    @Override
    public List<DocumentIndexResponse> getDocumentByMe(String query, int page, int size) {
        log.info("Get document by me: query={}, page={}, size={}", query, page, size);
        User currentUser = authenticationService.getCurrentUser();
        Set<Long> itemIdsAllowAccess = permissionService.getItemIdsByRecipientId(currentUser.getId());
        return documentIndexService.getDocumentShared(itemIdsAllowAccess, query, page, size);
    }
}
