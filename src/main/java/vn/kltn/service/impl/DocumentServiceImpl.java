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
import vn.kltn.entity.Folder;
import vn.kltn.entity.Tag;
import vn.kltn.entity.User;
import vn.kltn.exception.InvalidDataException;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.index.DocumentIndex;
import vn.kltn.repository.DocumentRepo;
import vn.kltn.service.*;
import vn.kltn.service.event.DocumentUpdatedEvent;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    private final UploadFinalizerService uploadFinalizerService;
    private final IUploadProcessor uploadProcessor;
    private final ApplicationEventPublisher eventPublisher;
    private final IDocumentStorageService documentStorageService;
    private final IDocumentMapperService documentMapperService;

    public DocumentServiceImpl(@Qualifier("documentPermissionServiceImpl") AbstractPermissionService abstractPermissionService, IFolderPermissionService folderPermissionService, DocumentRepo documentRepo  , IDocumentHasTagService documentHasTagService, IAuthenticationService authenticationService, FolderCommonService folderCommonService, IDocumentPermissionService documentPermissionService, IDocumentIndexService documentIndexService, UploadFinalizerService uploadFinalizerService, IUploadProcessor uploadProcessor, ApplicationEventPublisher eventPublisher, IDocumentStorageService documentStorageService, IDocumentMapperService documentMapperService) {
        super(documentPermissionService, folderPermissionService, authenticationService, abstractPermissionService, folderCommonService);
        this.documentRepo = documentRepo;
        this.documentHasTagService = documentHasTagService;
        this.documentIndexService = documentIndexService;
        this.documentPermissionService = documentPermissionService;
        this.uploadFinalizerService = uploadFinalizerService;
        this.uploadProcessor = uploadProcessor;
        this.eventPublisher = eventPublisher;
        this.documentStorageService = documentStorageService;
        this.documentMapperService = documentMapperService;
    }

    @Override
    @Async("taskExecutor")
    public void uploadDocumentWithoutParent(List<FileBuffer> bufferedFiles, CancellationToken token) {
        // luu db
        List<Document> documents = saveDocuments(bufferedFiles);
        // upload file to cloud
        processUpload(token, bufferedFiles, documents);
        // thong bao bang websocket
    }

    @Override
    @Async("taskExecutor")
    public void uploadDocumentWithParent(Long parentId, List<FileBuffer> bufferedFiles, CancellationToken token) {
        // luu db
        List<Document> documents = documentStorageService.saveDocumentsWithFolder(bufferedFiles, parentId);
        // storage file to cloud
        documentStorageService.store(token,bufferedFiles,documents);
        // thua ke quyen cua parent
        documentPermissionService.inheritPermissions(documents);
        // thong bao bang websocket

    }


    private List<DocumentIndex> handleIndexDocuments(List<Document> documents) {
        List<DocumentIndex> result = documentIndexService.insertAllDoc(documents).join();
        log.info("Document indexing completed with {} items", result.size());
        return result;
    }


    private void processUpload(CancellationToken token, List<FileBuffer> bufferedFiles, List<Document> documents) {
        if (uploadFinalizerService.checkCancelledAndFinalize(token, documents, null, null)) return;
        List<String> blobNames = uploadProcessor.process(bufferedFiles);
        if (uploadFinalizerService.checkCancelledAndFinalize(token, documents, null, blobNames)) return;
        mapBlobNamesToDocuments(documents, blobNames);
        if (uploadFinalizerService.checkCancelledAndFinalize(token, documents, null, blobNames)) return;
        List<DocumentIndex> documentIndices = handleIndexDocuments(documents);
        if (uploadFinalizerService.checkCancelledAndFinalize(token, documents, documentIndices, blobNames)) return;
        uploadFinalizerService.finalizeUpload(token);
    }


    private void mapBlobNamesToDocuments(List<Document> documents, List<String> blobNames) {
        for (int i = 0; i < documents.size(); i++) {
            documents.get(i).setBlobName(blobNames.get(i));
        }
    }

    private List<Document> saveDocuments(List<FileBuffer> fileBuffers) {
        List<Document> documents = mapFilesBufferToListDocument(fileBuffers);
        documents = documentRepo.saveAll(documents);
        return documents;
    }

    private List<Document> saveDocumentsWithFolder(List<FileBuffer> fileBuffers, Long folderId) {
        // Lưu tài liệu vào cơ sở dữ liệu
        List<Document> documents = mapFilesBufferToListDocument(fileBuffers);
        Folder folder = getFolderByIdOrThrow(folderId);
        documents.forEach(document -> document.setParent(folder));
        documents = documentRepo.saveAll(documents);
        return documents;
    }

    @Override
    protected void hardDeleteResource(Document resource) {
        log.info("hard delete document with id {}", resource.getId());
        documentStorageService.deleteBlob(resource.getBlobName());
        documentHasTagService.deleteAllByDocumentId(resource.getId());
        documentPermissionService.deletePermissionByResourceId(resource.getId());
        documentRepo.delete(resource);
        documentIndexService.deleteIndex(resource.getId().toString());
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
        documentIndexService.markDeleteDocument(document.getId().toString(), true);
    }

    @Override
    public DocumentResponse restoreResourceById(Long resourceId) {
        log.info("restore document with id {}", resourceId);
        Document resource = getResourceByIdOrThrow(resourceId);
        validateCurrentUserIsOwnerResource(resource);
        validateResourceDeleted(resource);
        resource.setDeletedAt(null);
        resource.setPermanentDeleteAt(null);
        documentIndexService.markDeleteDocument(resourceId.toString(), false);
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

    private List<Document> mapFilesBufferToListDocument(List<FileBuffer> bufferList) {
        List<Document> documents = new ArrayList<>();
        User uploader = authenticationService.getCurrentUser();
        // Lấy tên file upload
        List<String> listFileName = bufferList.stream().map(FileBuffer::getFileName).toList();

        // Tìm các document đã tồn tại theo tên
        List<Document> existingDocuments = documentRepo.findAllByListName(listFileName);

        // Map tên -> Document đã tồn tại, để tra nhanh
        Map<String, Document> existingMap = existingDocuments.stream().collect(Collectors.toMap(Document::getName, Function.identity(), (d1, d2) -> d1.getVersion() >= d2.getVersion() ? d1 : d2 // giữ document có version cao hơn
        ));


        // Duyệt từng file
        for (FileBuffer buffer : bufferList) {
            String fileName = buffer.getFileName();
            Document document = documentMapperService.mapFileBufferToDocument(buffer);
            document.setOwner(uploader);
            // Nếu file trùng tên với file cũ → tăng version
            if (existingMap.containsKey(fileName)) {
                int oldVersion = existingMap.get(fileName).getVersion();
                document.setVersion(oldVersion + 1);
            } else {
                document.setVersion(1); // File mới hoàn toàn
            }

            documents.add(document);
        }

        return documents;
    }


    @Override
    public void softDeleteDocumentsByFolderIds(List<Long> folderIds) {
        log.info("delete document by folder ids");
        documentRepo.setDeleteDocument(folderIds, LocalDateTime.now(), LocalDateTime.now().plusDays(documentRetentionDays));
        List<Long> documentsMarkDeleted = documentRepo.findDocumentIdsWithParentIds(folderIds);
        // danh dau isDeleted o elasticsearch
        markDeletedIndexByDocumentIds(documentsMarkDeleted, true);
    }

    @Override
    public void hardDeleteDocumentByFolderIds(List<Long> folderIds) {
        // Lấy danh sách documents theo folderIds
        List<Document> documentsToDelete = documentRepo.findDocumentsByParentIds(folderIds);

        if (documentsToDelete.isEmpty()) {
            return; // Không có document nào để xóa, thoát sớm
        }

        // Lấy danh sách blobName để xóa trên Azure
        List<String> blobNamesToDelete = documentsToDelete.stream().map(Document::getBlobName).filter(Objects::nonNull).toList();

        // Lấy danh sách documentId để xóa trong DB
        List<Long> documentIdsToDelete = documentsToDelete.stream().map(Document::getId).toList();

        // Xóa trên cloud
        documentStorageService.deleteBlobsFromCloud(blobNamesToDelete);
        // Xóa tags liên quan đến documents
        deleteTags(documentIdsToDelete);
        // Xóa documents khỏi database
        // vì để cascade là ALL với permission nên ko cần xóa thủ công
        deleteDocuments(documentIdsToDelete);
        // xóa trong elasticsearch
        deleteDataElasticSearch(documentIdsToDelete);
    }

    private void deleteDataElasticSearch(List<Long> documentIds) {
        if (!documentIds.isEmpty()) {
            documentIndexService.deleteIndexByIdList(documentIds);
        }
    }


    private void deleteTags(List<Long> documentIds) {
        if (!documentIds.isEmpty()) {
            documentHasTagService.deleteAllByDocumentIds(documentIds);
        }
    }

    private void deleteDocuments(List<Long> documentIds) {
        if (!documentIds.isEmpty()) {
            documentRepo.deleteAllById(documentIds);
        }
    }


    @Override
    public void restoreDocumentsByFolderIds(List<Long> folderIds) {
        documentRepo.setDeleteDocument(folderIds, null, null);
        List<Long> documentIdsToMarkDelete = documentRepo.findDocumentIdsWithParentIds(folderIds);
        markDeletedIndexByDocumentIds(documentIdsToMarkDelete, false);
    }

    @Override
    public List<DocumentIndexResponse> searchMetadata(String query, Pageable pageable) {
        log.info("search document by me");
        // Lấy danh sách documentId mà người dùng có quyền truy cập
        User currentUser = authenticationService.getCurrentUser();
        Set<Long> listDocShardedWithMe = documentPermissionService.getDocumentIdsByUser(currentUser.getId());
        return documentIndexService.getDocumentByMe(listDocShardedWithMe, query, pageable.getPageNumber(), pageable.getPageSize());
    }

    private void markDeletedIndexByDocumentIds(List<Long> documentIds, boolean value) {
        if (documentIds.isEmpty()) {
            return; // Không có document nào để đánh dấu, thoát sớm
        }
        // Chuyển đổi danh sách documentIds thành danh sách String
        List<String> documentIdsAsString = documentIds.stream().map(String::valueOf).toList();
        documentIndexService.markDeleteDocumentsIndex(documentIdsAsString, value);
    }

    @Override
    public DocumentDataResponse openDocumentById(Long documentId) {
        Document document = getResourceByIdOrThrow(documentId);
        return mapDocToDocDataResponse(document);
    }

    private DocumentDataResponse mapDocToDocDataResponse(Document document) {
        String blobName = document.getBlobName();
        try (InputStream inputStream = documentStorageService.downloadBlobInputStream(blobName)) {
            return DocumentDataResponse.builder().data(inputStream.readAllBytes()).name(document.getName() + document.getBlobName().substring(document.getBlobName().lastIndexOf('.'))).type(document.getType()).documentId(document.getId()).build();
        } catch (IOException e) {
            log.error("Error reading file from Azure Storage: {}", e.getMessage());
            throw new InvalidDataException("Lỗi đọc dữ liệu từ file");
        }
    }

    @Override
    public DocumentResponse copyDocumentById(Long documentId) {
        log.info("copy document by id: {}", documentId);
        Document document = getResourceByIdOrThrow(documentId);
        Document copied = copyDocument(document);
        return documentMapperService.mapToDocumentResponse(copied);
    }


    private Document copyDocument(Document document) {
        Document copied = documentMapperService.copyDocument(document);
        String nameCopy= document.getName().substring(0,document.getName().lastIndexOf(".")-1)+"_copy"+
                document.getName().substring(document.getName().lastIndexOf("."));
        copied.setName(nameCopy);
        documentRepo.save(copied);
        copied.setDeletedAt(null);
        copied.setPermanentDeleteAt(null);
        copyDocumentHasTag(document, copied);
        String blobDestinationCopied = documentStorageService.copyBlob(document.getBlobName());
        copied.setBlobName(blobDestinationCopied);
        // Lưu vào elasticsearch
        documentIndexService.insertDoc(copied);
        return documentRepo.save(copied);
    }


    private void copyDocumentHasTag(Document document, Document docCopy) {
        Set<Tag> tags = documentHasTagService.getTagsByDocumentId(document.getId());
        documentHasTagService.addDocumentToTag(docCopy, tags);
    }

    @Override
    public DocumentResponse updateDocumentById(Long documentId, DocumentRequest documentRequest) {
        log.info("update document by id: {}", documentId);
        Document docExists = getResourceByIdOrThrow(documentId);
        documentMapperService.updateDocument(docExists, documentRequest);
        docExists = documentRepo.save(docExists);
        eventPublisher.publishEvent(new DocumentUpdatedEvent(this,documentId));
        return documentMapperService.mapToDocumentResponse(docExists);
    }
}
