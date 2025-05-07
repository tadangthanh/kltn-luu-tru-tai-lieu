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
import vn.kltn.dto.response.*;
import vn.kltn.entity.*;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.index.DocumentIndex;
import vn.kltn.map.ItemMapper;
import vn.kltn.repository.DocumentRepo;
import vn.kltn.service.*;
import vn.kltn.service.event.DocumentUpdateContent;
import vn.kltn.service.event.DocumentUpdatedEvent;
import vn.kltn.util.DocumentTypeUtil;
import vn.kltn.util.ItemValidator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static vn.kltn.repository.util.FileUtil.getFileExtension;

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
    private final IAzureStorageService azureStorageService;
    private final IDocumentVersionService documentVersionService;

    public DocumentServiceImpl(DocumentRepo documentRepo, IDocumentHasTagService documentHasTagService, IAuthenticationService authenticationService, FolderCommonService folderCommonService, IDocumentIndexService documentIndexService, ApplicationEventPublisher eventPublisher, IDocumentStorageService documentStorageService, IDocumentMapperService documentMapperService, IDocumentSearchService documentSearchService, ItemValidator itemValidator, IPermissionInheritanceService permissionInheritanceService, IPermissionValidatorService permissionValidatorService, IPermissionService permissionService, ItemMapper itemMapper, IPermissionValidatorService permissionValidatorService1, WebSocketService webSocketService, IAzureStorageService azureStorageService, IDocumentVersionService documentVersionService) {
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
        this.azureStorageService = azureStorageService;
        this.documentVersionService = documentVersionService;
    }

    @Override
    @Async("taskExecutor")
    public void uploadDocumentEmptyParent(List<FileBuffer> bufferedFiles, CancellationToken token) {
        // luu db
        List<Document> documents = documentStorageService.saveDocuments(bufferedFiles);
//        documentVersionService.increaseVersions(documents);
        List<String> blobsName = new ArrayList<>();
        List<DocumentIndex> documentIndexList = new ArrayList<>();
        try {
            // upload file to cloud
            blobsName.addAll(documentStorageService.store(token, bufferedFiles, documents));
            documentMapperService.mapBlobNamesToDocuments(documents, blobsName);
            // luu index
            documentIndexList.addAll(documentIndexService.insertAllDoc(documents).join());

        } catch (Exception e) {
            log.error("Error uploading document: {}", e.getMessage());
            // Gửi thông báo lỗi về client
            webSocketService.sendUploadError(authenticationService.getCurrentUser().getEmail(), "Có lỗi xảy ra khi upload tài liệu.");
            documentStorageService.deleteBlobsFromCloud(blobsName);
            documentIndexService.deleteAll(documentIndexList);
            throw e;
        }
    }

    @Override
    public void updateDocumentEditor(Long documentId, byte[] data) {
        Document document = getItemByIdOrThrow(documentId);
        permissionValidatorService.validatePermissionManager(document, authenticationService.getCurrentUser());

        String originalFileName = document.getName();
        long fileSize = data.length;
        int chunkSize = 1024 * 1024; // 1MB
        String newBlobName;

        try (InputStream inputStream = new ByteArrayInputStream(data)) {
            newBlobName = azureStorageService.uploadChunkedWithContainerDefault(
                    inputStream, originalFileName, fileSize, chunkSize
            );
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi đọc dữ liệu tài liệu", e);
        }
        //  Tạo version mới thông qua service
        DocumentVersion newVersion = documentVersionService.createNewVersion(document, newBlobName, fileSize);
        document.setCurrentVersion(newVersion);
        documentRepo.save(document);

        log.info("Cập nhật documentId={} thành công với blobName={} và size={}", documentId, newBlobName, fileSize);
        eventPublisher.publishEvent(new DocumentUpdateContent(this, documentId));
    }


    @Override
    @Async("taskExecutor")
    public void uploadDocumentWithParent(Long parentId, List<FileBuffer> bufferedFiles, CancellationToken token) {
        User currentUser = authenticationService.getCurrentUser();
        List<String> blobsName = new ArrayList<>();
        List<DocumentIndex> documentIndexList = new ArrayList<>();
        try {
            log.info("upload document with parent id {}", parentId);
            Folder parent = folderCommonService.getFolderByIdOrThrow(parentId);
            permissionValidatorService.validatePermissionManager(parent, currentUser);
            List<Document> documents = documentStorageService.saveDocumentsWithFolder(bufferedFiles, parentId);
            // upload file to cloud
            blobsName.addAll(documentStorageService.store(token, bufferedFiles, documents));
            // map blobName to document
            documentMapperService.mapBlobNamesToDocuments(documents, blobsName);
            // luu index
            documentIndexList.addAll(documentIndexService.insertAllDoc(documents).join());
            // ke thua quyen cua parent
            permissionInheritanceService.inheritPermissionsFromParent(documents);
        } catch (AccessDeniedException e) {
            log.warn("Permission denied: {}", e.getMessage());
            // Có thể gửi message về client bằng WebSocket hoặc lưu log, hoặc trạng thái vào DB nếu cần
            webSocketService.sendUploadError(currentUser.getEmail(), "Bạn không có quyền upload vào thư mục này.");
        } catch (Exception e) {
            log.error("Error uploading document: {}", e.getMessage());
            // Gửi thông báo lỗi về client
            webSocketService.sendUploadError(currentUser.getEmail(), "Có lỗi xảy ra khi upload tài liệu.");
            documentStorageService.deleteBlobsFromCloud(blobsName);
            documentIndexService.deleteAll(documentIndexList);
        }
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
        return documentStorageService.download(document.getCurrentVersion().getBlobName());
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
    public OnlyOfficeConfig getOnlyOfficeConfig(Long documentId) {
        Document document = getItemByIdOrThrow(documentId);
        User currentUser = authenticationService.getCurrentUser();
        boolean isEdit = false;
        if (document.getOwner().getId().equals(currentUser.getId())) {
            isEdit = true;
        } else {
            // Kiểm tra quyền của người dùng hiện tại với tài liệu
            Permission permission = permissionService.getPermissionItemByRecipientId(document.getId(), currentUser.getId());
            isEdit = permission.getPermission().equals(vn.kltn.common.Permission.EDITOR);
        }
        // Lấy phần mở rộng file từ tên file
        String fileExtension = getFileExtension(document.getName());

        // Sử dụng hàm util để lấy fileType và documentType
        DocumentTypeUtil.DocumentTypeInfo documentTypeInfo = DocumentTypeUtil.getDocumentTypeInfo(fileExtension);
        // Tạo cấu hình cho OnlyOffice
        OnlyOfficeConfig config = new OnlyOfficeConfig();
        config.setDocumentId(document.getId());
        String documentKey = document.getId() + "-" + document.getUpdatedAt().getTime() + "-" + document.getCurrentVersion().getBlobName();
        config.setDocumentKey(documentKey);
        config.setDocumentTitle(document.getName());
        config.setFileType(documentTypeInfo.getFileType());
        config.setDocumentType(documentTypeInfo.getDocumentType());
//        config.setDocumentUrl(azureStorageService.getBlobUrl(document.getCurrentVersion().getBlobName())); // SAS URL để tải tài liệu
        config.setCallbackUrl("https://localhost:8080/api/v1/documents/save-editor");

        // Thông tin quyền truy cập người dùng
        OnlyOfficeConfig.Permissions permissions = new OnlyOfficeConfig.Permissions();
        permissions.setEdit(isEdit); // Quyền chỉnh sửa (có thể tùy chỉnh)
        permissions.setComment(true); // Quyền bình luận
        permissions.setDownload(false); // Quyền tải xuống

        config.setPermissions(permissions);

        // Thông tin người dùng
        OnlyOfficeConfig.User user = new OnlyOfficeConfig.User();
        user.setId(currentUser.getId().toString()); // Lấy từ context hoặc JWT của người dùng
        user.setName(currentUser.getFullName());
        config.setUser(user);
        return config;
    }

    @Override
    public void hardDeleteItemById(Long documentId) {
        log.info("hard delete document with id {}", documentId);
        Document resource = documentRepo.findById(documentId).orElse(null);
        if (resource == null) {
            return;
        }
        documentStorageService.deleteBlob(resource.getCurrentVersion().getBlobName());
        documentHasTagService.deleteAllByDocumentId(resource.getId());
        documentIndexService.deleteDocById(resource.getId());
        documentVersionService.deleteVersionsByDocumentId(documentId);
        documentRepo.delete(resource);
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
