package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.kltn.entity.Document;
import vn.kltn.entity.User;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.repository.DocumentRepo;
import vn.kltn.service.IAuthenticationService;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j(topic = "DOCUMENT_COMMON_SERVICE")
public class DocumentCommonService extends AbstractResourceCommonService<Document> {
    private final IAuthenticationService authenticationService;
    private final DocumentRepo documentRepo;

    @Override
    protected User getCurrentUser() {
        return authenticationService.getCurrentUser();
    }

    @Override
    protected Document getResourceByIdOrThrow(Long resourceId) {
        return documentRepo.findById(resourceId).orElseThrow(() -> {
            log.warn("Document with id {} not found", resourceId);
            return new ResourceNotFoundException("Không tìm thấy document");
        });
    }
}
