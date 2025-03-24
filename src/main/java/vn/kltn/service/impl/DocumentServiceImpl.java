package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.kltn.dto.request.DocumentRequest;
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
import vn.kltn.repository.util.PaginationUtils;
import vn.kltn.service.IAuthenticationService;
import vn.kltn.service.IAzureStorageService;
import vn.kltn.service.IDocumentHasTagService;
import vn.kltn.service.IDocumentService;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j(topic = "DOCUMENT_SERVICE")
public class DocumentServiceImpl implements IDocumentService {
    private final DocumentRepo documentRepo;
    private final DocumentMapper documentMapper;
    private final IAzureStorageService azureStorageService;
    private final IDocumentHasTagService documentHasTagService;
    private final IAuthenticationService authenticationService;
    private final FolderCommonService folderCommonService;
    @Value("${app.delete.document-retention-days}")
    private int documentRetentionDays;


    @Override
    public DocumentResponse uploadDocumentWithoutFolder(DocumentRequest documentRequest, MultipartFile file) {
        Document document = processValidDocument(documentRequest, file);
        return mapToDocumentResponse(document);
    }

    @Override
    public DocumentResponse uploadDocumentWithFolder(Long folderId, DocumentRequest documentRequest, MultipartFile file) {
        Document document = processValidDocument(documentRequest, file);
        Folder folder = folderCommonService.getFolderByIdOrThrow(folderId);
        document.setFolder(folder);
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
    public Document getDocumentByIdOrThrow(Long documentId) {
        return documentRepo.findById(documentId).orElseThrow(() -> {
            log.warn("Document with id {} not found", documentId);
            return new ResourceNotFoundException("Không tìm thấy document");
        });
    }

    @Override
    public PageResponse<List<DocumentResponse>> searchByCurrentUser(Pageable pageable, String[] documents) {
        log.info("search document by current user");
        if (documents != null && documents.length > 0) {
            EntitySpecificationsBuilder<Document> builder = new EntitySpecificationsBuilder<>();
            Pattern pattern = Pattern.compile("([a-zA-Z0-9_.]+?)([<:>~!])(.*)(\\p{Punct}?)(\\p{Punct}?)");
            for (String s : documents) {
                Matcher matcher = pattern.matcher(s);
                if (matcher.find()) {
                    builder.with(matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4), matcher.group(5));
                }
            }
            Specification<Document> spec = builder.build();
            // nó trả trả về 1 spec mới
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.isNull(root.get("deletedAt")));
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("createdBy"), email));
            Page<Document> docPage = documentRepo.findAll(spec, pageable);
            return PaginationUtils.convertToPageResponse(docPage, pageable, this::mapToDocumentResponse);
        }
        return PaginationUtils.convertToPageResponse(documentRepo.findAll(pageable), pageable, this::mapToDocumentResponse);
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
    public void softDeleteDocumentById(Long documentId) {
        Document document = getDocumentByIdOrThrow(documentId);
        validateDocumentNotDeleted(document);
        document.setDeletedAt(LocalDateTime.now());
        documentRepo.save(document);
    }

    @Override
    public void softDeleteDocumentsByFolderIds(List<Long> folderIds) {
        documentRepo.setDeleteDocument(folderIds, LocalDateTime.now(), LocalDateTime.now().plusDays(documentRetentionDays));
    }

    @Override
    public void hardDeleteDocumentByFolderIds(List<Long> folderIds) {
        // xóa blob trên azure trước
        // sau đó xóa document
        List<String> documentBlobsToDelete = documentRepo.getBlobNameDocumentsByFolderIds(folderIds);
        azureStorageService.deleteBLobs(documentBlobsToDelete);
        documentHasTagService.deleteAllByFolderIds(folderIds);
        documentRepo.deleteDocumentByListFolderId(folderIds);
    }

    @Override
    public void hardDeleteDocumentById(Long documentId) {
        Document document = getDocumentByIdOrThrow(documentId);
        validateDocumentDeleted(document);
        azureStorageService.deleteBlob(document.getBlobName());
        documentHasTagService.deleteAllByDocumentId(documentId);
        documentRepo.delete(document);
    }

    @Override
    public DocumentResponse restoreDocumentById(Long documentId) {
        Document document = getDocumentByIdOrThrow(documentId);
        validateDocumentDeleted(document);
        document.setDeletedAt(null);
        return mapToDocumentResponse(document);
    }

    @Override
    public DocumentResponse moveDocumentToFolder(Long documentId, Long folderId) {
        Document document = getDocumentByIdOrThrow(documentId);
        Folder folder = folderCommonService.getFolderByIdOrThrow(folderId);
        // xac nhan folder la cua user hien tai
        folderCommonService.validateCurrentUserIsOwnerFolder(folder);
        // xac nhan folder chua bi xoa
        folderCommonService.validateFolderNotDeleted(folder);
        // xac nhan document chua bi xoa
        validateDocumentNotDeleted(document);
        document.setFolder(folder);
        return mapToDocumentResponse(document);
    }

    private void validateDocumentDeleted(Document document) {
        if (document.getDeletedAt() == null) {
            throw new InvalidDataException("Document chưa bị xóa");
        }
    }

    private void validateDocumentNotDeleted(Document document) {
        if (document.getDeletedAt() != null) {
            throw new InvalidDataException("Document đã bị xóa");
        }
    }

    @Override
    public void restoreDocumentsByFolderIds(List<Long> folderIds) {
        documentRepo.setDeleteDocument(folderIds, null, null);
    }

    @Override
    public DocumentResponse copyDocumentById(Long documentId) {
        Document document = getDocumentByIdOrThrow(documentId);
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
