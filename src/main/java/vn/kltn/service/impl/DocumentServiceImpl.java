package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.kltn.dto.request.DocumentRequest;
import vn.kltn.dto.response.DocumentResponse;
import vn.kltn.entity.Document;
import vn.kltn.entity.Tag;
import vn.kltn.entity.User;
import vn.kltn.exception.InvalidDataException;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.map.DocumentMapper;
import vn.kltn.repository.DocumentRepo;
import vn.kltn.service.IAuthenticationService;
import vn.kltn.service.IAzureStorageService;
import vn.kltn.service.IDocumentHasTagService;
import vn.kltn.service.IDocumentService;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

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


    @Override
    public DocumentResponse uploadDocumentWithoutFolder(DocumentRequest documentRequest, MultipartFile file) {
        Document document = processValidDocument(documentRequest, file);
        return mapToDocumentResponse(document);
    }

    private Document processValidDocument(DocumentRequest documentRequest, MultipartFile file) {
        Document document = mapToDocument(documentRequest, file);
        document.setVersion(1);
        document = documentRepo.save(document);
        User uploader = authenticationService.getCurrentUser();
        document.setOwner(uploader);
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
        document.setDeletedAt(LocalDateTime.now());
        documentRepo.save(document);
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
