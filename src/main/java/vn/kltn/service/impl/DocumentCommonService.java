package vn.kltn.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import vn.kltn.dto.response.DocumentResponse;
import vn.kltn.entity.Document;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.map.DocumentMapper;
import vn.kltn.repository.DocumentRepo;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "DOCUMENT_COMMON_SERVICE")
public class DocumentCommonService {
    private final DocumentRepo documentRepo;
    private final DocumentMapper documentMapper;

    public Document getDocumentByIdOrThrow(Long documentId) {
        return documentRepo.findById(documentId).orElseThrow(() -> {
            log.warn("Document with id {} not found", documentId);
            return new ResourceNotFoundException("Không tìm thấy document");
        });
    }

    public Page<Document> getPageDocumentBySpec(Specification<Document> spec, Pageable pageable) {
        return documentRepo.findAll(spec, pageable);
    }

    public DocumentResponse mapToDocumentResponse(Document document) {
        return documentMapper.toDocumentResponse(document);
    }

    public List<Long> getDocumentChildIdsEmptyPermission(List<Long> parentResourceIds, Long recipientId) {
        log.info("Get document child ids without permission: parentResourceIds={}, recipientId={}", parentResourceIds, recipientId);
        return documentRepo.findDocumentChildIdsEmptyPermission(parentResourceIds, recipientId);
    }

    public List<Document> getDocuments(List<Long> documentChildIds) {
        return documentRepo.findAllById(documentChildIds);
    }
}
