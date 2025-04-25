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
import vn.kltn.util.ItemValidator;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@Slf4j(topic = "DOCUMENT_SERVICE")
public class DocumentCommonServiceImpl extends AbstractItemCommonService<Document, DocumentResponse> implements IDocumentCommonService {
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
    private final ItemValidator itemValidator;

    public DocumentCommonServiceImpl(@Qualifier("documentPermissionServiceImpl") AbstractPermissionService abstractPermissionService, IFolderPermissionService folderPermissionService, DocumentRepo documentRepo, IDocumentHasTagService documentHasTagService, IAuthenticationService authenticationService, FolderCommonService folderCommonService, IDocumentPermissionService documentPermissionService, IDocumentIndexService documentIndexService, ApplicationEventPublisher eventPublisher, IDocumentStorageService documentStorageService, IDocumentMapperService documentMapperService, IDocumentSearchService documentSearchService, ItemValidator itemValidator) {
        super(documentPermissionService, folderPermissionService, authenticationService, abstractPermissionService, folderCommonService,itemValidator);
        this.documentRepo = documentRepo;
        this.documentHasTagService = documentHasTagService;
        this.documentIndexService = documentIndexService;
        this.documentPermissionService = documentPermissionService;
        this.eventPublisher = eventPublisher;
        this.documentStorageService = documentStorageService;
        this.documentMapperService = documentMapperService;
        this.documentSearchService = documentSearchService;
        this.itemValidator = itemValidator;
    }

    @Override
    @Async("taskExecutor")
    public void uploadDocumentEmptyParent(List<FileBuffer> bufferedFiles, CancellationToken token) {
        // luu db
        List<Document> documents = documentStorageService.saveDocuments(bufferedFiles);
        // upload file to cloud
        documentStorageService.store(token, bufferedFiles, documents);
    }

    @Override
    @Async("taskExecutor")
    public void uploadDocumentWithParent(Long parentId, List<FileBuffer> bufferedFiles, CancellationToken token) {
        // luu db
        List<Document> documents = documentStorageService.saveDocumentsWithFolder(bufferedFiles, parentId);
        // storage file to cloud
        documentStorageService.store(token, bufferedFiles, documents);
        // thua ke quyen cua parent
        documentPermissionService.inheritPermissionsFromParent(documents);
        // thong bao bang websocket

    }

    @Override
    protected void hardDeleteResource(Document resource) {
        log.info("hard delete document with id {}", resource.getId());
        documentStorageService.deleteBlob(resource.getBlobName());
        documentHasTagService.deleteAllByDocumentId(resource.getId());
        documentPermissionService.deletePermissionByResourceId(resource.getId());
        documentIndexService.deleteDocById(resource.getId());
        documentRepo.delete(resource);
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
    public DocumentResponse restoreItemById(Long itemId) {
        log.info("restore document with id {}", itemId);
        Document resource = getItemByIdOrThrow(itemId);
        itemValidator.validateCurrentUserIsOwnerItem(resource);
        itemValidator.validateItemDeleted(resource);
        resource.setDeletedAt(null);
        resource.setPermanentDeleteAt(null);
        documentIndexService.markDeleteDocument(itemId, false);
        return mapToR(resource);
    }

    @Override
    public Document getItemByIdOrThrow(Long itemId) {
        return documentRepo.findById(itemId).orElseThrow(() -> {
            log.warn("Resource not found with id {}", itemId);
            return new ResourceNotFoundException("Resource not found with id " + itemId);
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
        Document document = getItemByIdOrThrow(documentId);
        return documentMapperService.mapDocToDocDataResponse(document);
    }


    @Override
    public DocumentResponse copyDocumentById(Long documentId) {
        log.info("copy document by id: {}", documentId);
        Document document = getItemByIdOrThrow(documentId);
        Document copied = documentStorageService.copyDocument(document);
        return documentMapperService.mapToDocumentResponse(copied);
    }


    @Override
    public DocumentResponse updateDocumentById(Long documentId, DocumentRequest documentRequest) {
        log.info("update document by id: {}", documentId);
        Document docExists = getItemByIdOrThrow(documentId);
        documentMapperService.updateDocument(docExists, documentRequest);
        docExists = documentRepo.save(docExists);
        eventPublisher.publishEvent(new DocumentUpdatedEvent(this, documentId));
        return documentMapperService.mapToDocumentResponse(docExists);
    }
}
