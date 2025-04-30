package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import vn.kltn.common.CancellationToken;
import vn.kltn.dto.FileBuffer;
import vn.kltn.dto.request.DocumentRequest;
import vn.kltn.dto.response.DocumentDataResponse;
import vn.kltn.dto.response.DocumentIndexResponse;
import vn.kltn.dto.response.DocumentResponse;
import vn.kltn.dto.response.ItemResponse;
import vn.kltn.entity.Document;
import vn.kltn.entity.Folder;
import vn.kltn.entity.User;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.map.ItemMapper;
import vn.kltn.repository.DocumentRepo;
import vn.kltn.service.*;
import vn.kltn.service.event.DocumentUpdatedEvent;
import vn.kltn.util.ItemValidator;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@Slf4j(topic = "DOCUMENT_SERVICE")
public class DocumentServiceImpl extends AbstractItemCommonService<Document, DocumentResponse> implements IDocumentService {
    private final DocumentRepo documentRepo;
    private final IDocumentHasTagService documentHasTagService;
    @Value("${app.delete.document-retention-days}")
    private int documentRetentionDays;
    private final IDocumentIndexService documentIndexService;
    private final ApplicationEventPublisher eventPublisher;
    private final IDocumentStorageService documentStorageService;
    private final IDocumentMapperService documentMapperService;
    private final IDocumentSearchService documentSearchService;
    private final ItemValidator itemValidator;
    private final IPermissionService permissionService;
    private final ItemMapper itemMapper;
    private final IPermissionValidatorService permissionValidatorService;
    private final WebSocketService webSocketService;
    public DocumentServiceImpl(DocumentRepo documentRepo, IDocumentHasTagService documentHasTagService, IAuthenticationService authenticationService, FolderCommonService folderCommonService, IDocumentIndexService documentIndexService, ApplicationEventPublisher eventPublisher, IDocumentStorageService documentStorageService, IDocumentMapperService documentMapperService, IDocumentSearchService documentSearchService, ItemValidator itemValidator, IPermissionInheritanceService permissionInheritanceService, IPermissionValidatorService permissionValidatorService, IPermissionService permissionService, ItemMapper itemMapper, IPermissionValidatorService permissionValidatorService1, WebSocketService webSocketService) {
        super(authenticationService, folderCommonService, itemValidator, permissionInheritanceService, permissionValidatorService, permissionService);
        this.documentRepo = documentRepo;
        this.documentHasTagService = documentHasTagService;
        this.documentIndexService = documentIndexService;
        this.permissionService = permissionService;
        this.eventPublisher = eventPublisher;
        this.documentStorageService = documentStorageService;
        this.documentMapperService = documentMapperService;
        this.documentSearchService = documentSearchService;
        this.itemValidator = itemValidator;
        this.itemMapper = itemMapper;
        this.permissionValidatorService = permissionValidatorService1;
        this.webSocketService = webSocketService;
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
        User currentUser = authenticationService.getCurrentUser();
        try {
            log.info("upload document with parent id {}", parentId);
            Folder parent = folderCommonService.getFolderByIdOrThrow(parentId);
            permissionValidatorService.validatePermissionManager(parent, currentUser);

            List<Document> documents = documentStorageService.saveDocumentsWithFolder(bufferedFiles, parentId);
            documentStorageService.store(token, bufferedFiles, documents);
            permissionInheritanceService.inheritPermissionsFromParent(documents);

            // Gửi websocket hoặc notification nếu cần

        } catch (AccessDeniedException e) {
            log.warn("Permission denied: {}", e.getMessage());
            // Có thể gửi message về client bằng WebSocket hoặc lưu log, hoặc trạng thái vào DB nếu cần
            webSocketService.sendUploadError(currentUser.getEmail(), "Bạn không có quyền upload vào thư mục này.");

        }
    }


    @Override
    protected void hardDeleteResource(Document resource) {
        log.info("hard delete document with id {}", resource.getId());
        documentStorageService.deleteBlob(resource.getBlobName());
        documentHasTagService.deleteAllByDocumentId(resource.getId());
        permissionService.deletePermissionByItemId(resource.getId());
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
    public void softDeleteDocumentById(Long documentId) {
        log.info("soft delete document with id {}", documentId);
        Document document = getItemByIdOrThrow(documentId);
        document.setDeletedAt(LocalDateTime.now());
        document.setPermanentDeleteAt(LocalDateTime.now().plusDays(documentRetentionDays));
        documentIndexService.markDeleteDocument(document.getId(), true);
    }

    @Override
    public InputStream download(Long documentId) {
        Document document = getItemByIdOrThrow(documentId);
        return documentStorageService.download(document.getBlobName());
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
    public ItemResponse copyDocumentById(Long documentId) {
        log.info("copy document by id: {}", documentId);
        Document document = getItemByIdOrThrow(documentId);
        Document copied = documentStorageService.copyDocument(document);
        return itemMapper.toResponse(copied);
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
