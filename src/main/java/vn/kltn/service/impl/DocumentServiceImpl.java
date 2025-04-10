package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.kltn.dto.request.DocumentRequest;
import vn.kltn.dto.response.DocumentDataResponse;
import vn.kltn.dto.response.DocumentResponse;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.entity.Document;
import vn.kltn.entity.Folder;
import vn.kltn.entity.Tag;
import vn.kltn.entity.User;
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
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

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
    private final DocumentIndexService documentIndexService;

    public DocumentServiceImpl(@Qualifier("documentPermissionServiceImpl") AbstractPermissionService abstractPermissionService, IFolderPermissionService folderPermissionService, DocumentRepo documentRepo, DocumentMapper documentMapper, IAzureStorageService azureStorageService, IDocumentHasTagService documentHasTagService, IAuthenticationService authenticationService, FolderCommonService folderCommonService, IDocumentPermissionService documentPermissionService, DocumentIndexService documentIndexService) {
        super(documentPermissionService, folderPermissionService, authenticationService, abstractPermissionService, folderCommonService);
        this.documentRepo = documentRepo;
        this.documentMapper = documentMapper;
        this.azureStorageService = azureStorageService;
        this.documentHasTagService = documentHasTagService;
        this.folderCommonService = folderCommonService;
        this.documentIndexService = documentIndexService;
    }

    @Override
    public DocumentResponse uploadDocumentWithoutParent(DocumentRequest documentRequest, MultipartFile file) {
        Document document = processValidDocument(documentRequest, file);
        return mapToDocumentResponse(document);
    }

    @Override
    public DocumentResponse uploadDocumentWithParent(Long parentId, DocumentRequest documentRequest, MultipartFile file) {
        Document document = processValidDocument(documentRequest, file);
        Folder folder = folderCommonService.getFolderByIdOrThrow(parentId);
        document.setParent(folder);
        document = documentRepo.save(document);
        // document moi tao se thua ke cac quyen tu folder cha
        documentPermissionService.inheritPermissions(document);
        return mapToDocumentResponse(document);
    }


    private Document processValidDocument(DocumentRequest documentRequest, MultipartFile file) {
        Document document = mapToDocument(documentRequest, file);
        document.setVersion(1);
        User uploader = authenticationService.getCurrentUser();
        document.setOwner(uploader);
        document = documentRepo.save(document);
        documentHasTagService.addDocumentToTag(document, documentRequest.getTags());
        // upload file to cloud
        String blobName = uploadDocumentToCloud(file);
        document.setBlobName(blobName);
        return document;
    }

    private String uploadDocumentToCloud(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            return azureStorageService.uploadChunkedWithContainerDefault(inputStream, file.getOriginalFilename(), file.getSize(), 10 * 1024 * 1024);
        } catch (IOException e) {
            throw new InvalidDataException(e.getMessage());
        }
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
    }

    @Override
    public DocumentResponse restoreResourceById(Long resourceId) {
        log.info("restore document with id {}", resourceId);
        Document resource = getResourceByIdOrThrow(resourceId);
        validateCurrentUserIsOwnerResource(resource);
        validateResourceDeleted(resource);
        resource.setDeletedAt(null);
        resource.setPermanentDeleteAt(null);
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


    private Document mapToDocument(DocumentRequest documentRequest, MultipartFile file) {
        Document document = documentMapper.toDocument(documentRequest);
        document.setSize(file.getSize());
        document.setType(file.getContentType());
        document.setName(file.getOriginalFilename());
        return document;
    }

    private DocumentResponse mapToDocumentResponse(Document document) {
        return documentMapper.toDocumentResponse(document);
    }

    @Override
    public void softDeleteDocumentsByFolderIds(List<Long> folderIds) {
        log.info("delete document by folder ids");
        documentRepo.setDeleteDocument(folderIds, LocalDateTime.now(), LocalDateTime.now().plusDays(documentRetentionDays));
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
    }

    @Override
    public DocumentDataResponse openDocumentById(Long documentId) {
        Document document = getResourceByIdOrThrow(documentId);
        return mapDocToDocDataResponse(document);
    }

    @Override
    public void indexDocumentById(Long documentId) {
        Document document = getResourceByIdOrThrow(documentId);
        documentIndexService.indexDocument(document, azureStorageService.downloadBlobInputStream(document.getBlobName()));
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
        return null;
    }

}
