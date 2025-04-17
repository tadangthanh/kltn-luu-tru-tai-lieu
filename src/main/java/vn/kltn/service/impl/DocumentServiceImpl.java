package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import vn.kltn.common.CancellationToken;
import vn.kltn.dto.FileBuffer;
import vn.kltn.dto.request.DocumentRequest;
import vn.kltn.dto.response.DocumentDataResponse;
import vn.kltn.dto.response.DocumentIndexResponse;
import vn.kltn.dto.response.DocumentResponse;
import vn.kltn.entity.Document;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.repository.DocumentRepo;
import vn.kltn.service.*;
import vn.kltn.service.event.DocumentUpdatedEvent;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@Slf4j(topic = "DOCUMENT_SERVICE")
public class DocumentServiceImpl extends AbstractResourceService<Document, DocumentResponse> implements IDocumentService {
    private final DocumentRepo documentRepo;
    private final IDocumentHasTagService documentHasTagService;
    @Value("${app.delete.document-retention-days}")
    private int documentRetentionDays;
    private final IDocumentIndexService documentIndexService;
    private final IDocumentPermissionService documentPermissionService;
    private final ApplicationEventPublisher eventPublisher;
    private final IDocumentStorageService documentStorageService;
    private final IDocumentMapperService documentMapperService;
    private final IDocumentSearchService documentSearchService;

    public DocumentServiceImpl(@Qualifier("documentPermissionServiceImpl") AbstractPermissionService abstractPermissionService, IFolderPermissionService folderPermissionService, DocumentRepo documentRepo, IDocumentHasTagService documentHasTagService, IAuthenticationService authenticationService, FolderCommonService folderCommonService, IDocumentPermissionService documentPermissionService, IDocumentIndexService documentIndexService, ApplicationEventPublisher eventPublisher, IDocumentStorageService documentStorageService, IDocumentMapperService documentMapperService, IDocumentSearchService documentSearchService) {
        super(documentPermissionService, folderPermissionService, authenticationService, abstractPermissionService, folderCommonService);
        this.documentRepo = documentRepo;
        this.documentHasTagService = documentHasTagService;
        this.documentIndexService = documentIndexService;
        this.documentPermissionService = documentPermissionService;
        this.eventPublisher = eventPublisher;
        this.documentStorageService = documentStorageService;
        this.documentMapperService = documentMapperService;
        this.documentSearchService = documentSearchService;
    }

    @Override
    @Async("taskExecutor")
    public void uploadDocumentEmptyParent(List<FileBuffer> bufferedFiles, CancellationToken token) {
        // luu db
        List<Document> documents = documentStorageService.saveDocuments(bufferedFiles);
        // upload file to cloud
        documentStorageService.store(token, bufferedFiles, documents);
        // thong bao bang websocket
    }

    @Override
    @Async("taskExecutor")
    public void uploadDocumentWithParent(Long parentId, List<FileBuffer> bufferedFiles, CancellationToken token) {
        // luu db
        List<Document> documents = documentStorageService.saveDocumentsWithFolder(bufferedFiles, parentId);
        // storage file to cloud
        documentStorageService.store(token, bufferedFiles, documents);
        // thua ke quyen cua parent
        documentPermissionService.inheritPermissions(documents);
        // thong bao bang websocket

    }

    @Override
    protected void hardDeleteResource(Document resource) {
        log.info("hard delete document with id {}", resource.getId());
        documentStorageService.deleteBlob(resource.getBlobName());
        documentHasTagService.deleteAllByDocumentId(resource.getId());
        documentPermissionService.deletePermissionByResourceId(resource.getId());
        documentRepo.delete(resource);
        documentIndexService.deleteDocById(resource.getId());
    }

    @Override
    protected Page<Document> getPageResource(Pageable pageable) {
        log.info("get page document");
        return documentRepo.findAll(pageable);
    }

    @Override
    protected Page<Document> getPageResourceBySpec(Specification<Document> spec, Pageable pageable) {
        log.info("get page document by specification");
        return documentRepo.findAll(spec, pageable);
    }

    @Override
    protected DocumentResponse mapToR(Document resource) {
        return documentMapperService.mapToDocumentResponse(resource);
    }

    @Override
    protected void softDeleteResource(Document document) {
        log.info("soft delete document with id {}", document.getId());
        document.setDeletedAt(LocalDateTime.now());
        document.setPermanentDeleteAt(LocalDateTime.now().plusDays(documentRetentionDays));
        documentIndexService.markDeleteDocument(document.getId(), true);
    }

    @Override
    public DocumentResponse restoreResourceById(Long resourceId) {
        log.info("restore document with id {}", resourceId);
        Document resource = getResourceByIdOrThrow(resourceId);
        validateCurrentUserIsOwnerResource(resource);
        validateResourceDeleted(resource);
        resource.setDeletedAt(null);
        resource.setPermanentDeleteAt(null);
        documentIndexService.markDeleteDocument(resourceId, false);
        return mapToR(resource);
    }

    @Override
    public Document getResourceByIdOrThrow(Long resourceId) {
        return documentRepo.findById(resourceId).orElseThrow(() -> {
            log.warn("Resource not found with id {}", resourceId);
            return new ResourceNotFoundException("Resource not found with id " + resourceId);
        });
    }


    @Override
    protected Document saveResource(Document resource) {
        return documentRepo.save(resource);
    }

    @Override
    public void softDeleteDocumentsByFolderIds(List<Long> folderIds) {
        log.info("delete document by folder ids");
        documentStorageService.markDeleteDocumentsByFolders(folderIds);
    }

    @Override
    public void hardDeleteDocumentByFolderIds(List<Long> folderIds) {
        documentStorageService.deleteDocumentsByFolders(folderIds);
    }


    @Override
    public void restoreDocumentsByFolderIds(List<Long> folderIds) {
        documentStorageService.restoreDocumentsByFolders(folderIds);
    }

    @Override
    public List<DocumentIndexResponse> searchMetadata(String query, Pageable pageable) {
        return documentSearchService.getDocumentByMe(query, pageable.getPageNumber(), pageable.getPageSize());
    }

    @Override
    public DocumentDataResponse openDocumentById(Long documentId) {
        Document document = getResourceByIdOrThrow(documentId);
        return documentMapperService.mapDocToDocDataResponse(document);
    }


    @Override
    public DocumentResponse copyDocumentById(Long documentId) {
        log.info("copy document by id: {}", documentId);
        Document document = getResourceByIdOrThrow(documentId);
        Document copied = documentStorageService.copyDocument(document);
        return documentMapperService.mapToDocumentResponse(copied);
    }


    @Override
    public DocumentResponse updateDocumentById(Long documentId, DocumentRequest documentRequest) {
        log.info("update document by id: {}", documentId);
        Document docExists = getResourceByIdOrThrow(documentId);
        documentMapperService.updateDocument(docExists, documentRequest);
        docExists = documentRepo.save(docExists);
        eventPublisher.publishEvent(new DocumentUpdatedEvent(this, documentId));
        return documentMapperService.mapToDocumentResponse(docExists);
    }
}
