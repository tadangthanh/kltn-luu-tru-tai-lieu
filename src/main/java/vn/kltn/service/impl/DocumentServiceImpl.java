package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.kltn.dto.request.DocumentRequest;
import vn.kltn.dto.response.DocumentDataResponse;
import vn.kltn.dto.response.DocumentIndexResponse;
import vn.kltn.dto.response.DocumentResponse;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.entity.Document;
import vn.kltn.entity.Folder;
import vn.kltn.entity.Tag;
import vn.kltn.entity.User;
import vn.kltn.exception.CustomIOException;
import vn.kltn.exception.InvalidDataException;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.map.DocumentMapper;
import vn.kltn.repository.DocumentRepo;
import vn.kltn.repository.specification.EntitySpecificationsBuilder;
import vn.kltn.repository.specification.SpecificationUtil;
import vn.kltn.repository.util.PaginationUtils;
import vn.kltn.service.*;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j(topic = "DOCUMENT_SERVICE")
public class DocumentServiceImpl extends AbstractResourceService<Document, DocumentResponse> implements IDocumentService {
    private final DocumentRepo documentRepo;
    private final DocumentMapper documentMapper;
    private final IAzureStorageService azureStorageService;
    private final IDocumentHasTagService documentHasTagService;
    @Value("${app.delete.document-retention-days}")
    private int documentRetentionDays;
    private final FolderCommonService folderCommonService;
    private final IDocumentIndexService documentIndexService;
    private final IDocumentPermissionService documentPermissionService;

    public DocumentServiceImpl(@Qualifier("documentPermissionServiceImpl") AbstractPermissionService abstractPermissionService, IFolderPermissionService folderPermissionService, DocumentRepo documentRepo, DocumentMapper documentMapper, IAzureStorageService azureStorageService, IDocumentHasTagService documentHasTagService, IAuthenticationService authenticationService, FolderCommonService folderCommonService, IDocumentPermissionService documentPermissionService, IDocumentIndexService documentIndexService, IDocumentPermissionService documentPermissionService1) {
        super(documentPermissionService, folderPermissionService, authenticationService, abstractPermissionService, folderCommonService);
        this.documentRepo = documentRepo;
        this.documentMapper = documentMapper;
        this.azureStorageService = azureStorageService;
        this.documentHasTagService = documentHasTagService;
        this.folderCommonService = folderCommonService;
        this.documentIndexService = documentIndexService;
        this.documentPermissionService = documentPermissionService1;
    }

    @Override
    @Async("taskExecutor")
    public void uploadDocumentWithoutParent(MultipartFile[] files) {
        List<Document> documents = saveDocuments(files);
        // upload file to cloud
        List<String> blobNames = uploadDocumentToCloud(files);
        for (int i = 0; i < documents.size(); i++) {
            Document document = documents.get(i);
            String blobName = blobNames.get(i);
            document.setBlobName(blobName);
        }
        documentIndexService.insertAllDoc(documents);
        // thong bao bang websocket
//        return mapToDocumentResponse(documents);
    }

    @Override
    public DocumentResponse uploadDocumentWithParent(Long parentId, DocumentRequest documentRequest, MultipartFile file) {
//        List<Document> documents = saveDocuments(files);
//        Folder folder = folderCommonService.getFolderByIdOrThrow(parentId);
//        documents.setParent(folder);
//        documents = documentRepo.save(documents);
//        // document moi tao se thua ke cac quyen tu folder cha
//        documentPermissionService.inheritPermissions(documents);
//        documentIndexService.insertDoc(documents, azureStorageService.downloadBlobInputStream(documents.getBlobName()));
//        return mapToDocumentResponse(documents);
        return null;
    }


    private List<Document> saveDocuments(MultipartFile[] files) {
        List<Document> documents = mapListFileToListDocument(files);
        documents = documentRepo.saveAll(documents);
//        // upload file to cloud
//        String blobName = uploadDocumentToCloud(file);
//        document.setBlobName(blobName);
        return documents;
    }

    private List<String> uploadDocumentToCloud(MultipartFile[] files) {
        List<CompletableFuture<String>> futures = new ArrayList<>();

        for (MultipartFile file : files) {
            try (InputStream inputStream = file.getInputStream()) {
                CompletableFuture<String> future = azureStorageService
                        .uploadChunkedWithContainerDefaultAsync(inputStream, file.getOriginalFilename(), file.getSize(), 10 * 1024 * 1024);
                futures.add(future);
            } catch (IOException e) {
                log.error("Error reading file: {}", e.getMessage());
                throw new CustomIOException("Có lỗi xảy ra khi đọc file");
            }
        }

        // Gộp tất cả future lại
        CompletableFuture<Void> allDoneFuture = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        // Đợi tất cả hoàn tất rồi collect kết quả
        CompletableFuture<List<String>> resultsFuture = allDoneFuture.thenApply(v ->
                futures.stream().map(CompletableFuture::join).toList()
        );

        return resultsFuture.join(); // ✅ chỉ block 1 lần ở đây khi đã xong hết
    }


    @Override
    public PageResponse<List<DocumentResponse>> searchByCurrentUser(Pageable pageable, String[] documents) {
        log.info("search document by current user");
        if (documents != null && documents.length > 0) {
            EntitySpecificationsBuilder<Document> builder = new EntitySpecificationsBuilder<>();
            Specification<Document> spec = SpecificationUtil.buildSpecificationFromFilters(documents, builder);
            // nó trả trả về 1 spec mới
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.isNull(root.get("deletedAt")));
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("createdBy"), email));
            Page<Document> docPage = documentRepo.findAll(spec, pageable);
            return PaginationUtils.convertToPageResponse(docPage, pageable, this::mapToDocumentResponse);
        }
        return PaginationUtils.convertToPageResponse(documentRepo.findAll(pageable), pageable, this::mapToDocumentResponse);
    }

    @Override
    protected void hardDeleteResource(Document resource) {
        log.info("hard delete document with id {}", resource.getId());
        azureStorageService.deleteBlob(resource.getBlobName());
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
        return documentMapper.toDocumentResponse(resource);
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

    private List<Document> mapListFileToListDocument(MultipartFile[] files) {
        List<Document> documents = new ArrayList<>();
        User uploader = authenticationService.getCurrentUser();
        // Lấy tên file upload
        List<String> listFileName = Arrays.stream(files)
                .map(MultipartFile::getOriginalFilename)
                .toList();

        // Tìm các document đã tồn tại theo tên
        List<Document> existingDocuments = documentRepo.findAllByListName(listFileName);

        // Map tên -> Document đã tồn tại, để tra nhanh
        Map<String, Document> existingMap = existingDocuments.stream()
                .collect(Collectors.toMap(Document::getName, Function.identity()));

        // Duyệt từng file
        for (MultipartFile file : files) {
            String fileName = file.getOriginalFilename();
            Document document = new Document();
            document.setSize(file.getSize());
            document.setType(file.getContentType());
            document.setName(fileName);
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


    private Document mapToDocument(DocumentRequest documentRequest, MultipartFile file) {
        Document document = documentMapper.toDocument(documentRequest);
        document.setSize(file.getSize());
        document.setType(file.getContentType());
        document.setName(file.getOriginalFilename());
        return document;
    }

    private List<DocumentResponse> mapToDocumentResponse(List<Document> documents) {
        return documents.stream()
                .map(this::mapToDocumentResponse)
                .toList();
    }

    private DocumentResponse mapToDocumentResponse(Document document) {
        return documentMapper.toDocumentResponse(document);
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
        deleteBlobsFromCloud(blobNamesToDelete);
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

    private void deleteBlobsFromCloud(List<String> blobNames) {
        if (!blobNames.isEmpty()) {
            azureStorageService.deleteBLobs(blobNames);
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
        try (InputStream inputStream = azureStorageService.downloadBlobInputStream(blobName)) {
            return DocumentDataResponse.builder().data(inputStream.readAllBytes()).name(document.getName() + document.getBlobName().substring(document.getBlobName().lastIndexOf('.'))).type(document.getType()).documentId(document.getId()).build();
        } catch (IOException e) {
            log.error("Error reading file from Azure Storage: {}", e.getMessage());
            throw new InvalidDataException("Lỗi đọc dữ liệu từ file");
        }
    }

    @Override
    public DocumentResponse copyDocumentById(Long documentId) {
        Document document = getResourceByIdOrThrow(documentId);
        Document copied = copyDocument(document);
        return mapToDocumentResponse(copied);
    }


    private Document copyDocument(Document document) {
        Document copied = documentMapper.copyDocument(document);
        documentRepo.save(copied);
        copied.setDeletedAt(null);
        copied.setPermanentDeleteAt(null);
        copyDocumentHasTag(document, copied);
        String blobDestinationCopied = azureStorageService.copyBlob(document.getBlobName(), generateBlobDestinationFromDocSource(document));
        copied.setBlobName(blobDestinationCopied);
        return copied;
    }

    private String generateBlobDestinationFromDocSource(Document documentSource) {
        String uuid = UUID.randomUUID().toString();
        String name = documentSource.getName();
        String extension = documentSource.getBlobName().substring(documentSource.getBlobName().lastIndexOf("."));
        return uuid + "_" + name + "." + extension;
    }

    private void copyDocumentHasTag(Document document, Document docCopy) {
        Set<Tag> tags = documentHasTagService.getTagsByDocumentId(document.getId());
        documentHasTagService.addDocumentToTag(docCopy, tags);
    }

    @Override
    public DocumentResponse updateDocumentById(Long documentId, DocumentRequest documentRequest) {
        log.info("update document by id: {}", documentId);
        Document docExists = getResourceByIdOrThrow(documentId);
        documentMapper.updateDocument(docExists, documentRequest);
        docExists = documentRepo.save(docExists);
        documentIndexService.updateDocument(docExists);
        return mapToDocumentResponse(docExists);
    }
}
