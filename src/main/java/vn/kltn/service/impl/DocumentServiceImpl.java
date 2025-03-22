package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.kltn.dto.request.DocumentRequest;
import vn.kltn.dto.response.DocumentResponse;
import vn.kltn.entity.Document;
import vn.kltn.exception.InvalidDataException;
import vn.kltn.map.DocumentMapper;
import vn.kltn.repository.DocumentRepo;
import vn.kltn.service.IAzureStorageService;
import vn.kltn.service.IDocumentHasTagService;
import vn.kltn.service.IDocumentService;

import java.io.IOException;
import java.io.InputStream;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j(topic = "DOCUMENT_SERVICE")
public class DocumentServiceImpl implements IDocumentService {
    private final DocumentRepo documentRepo;
    private final DocumentMapper documentMapper;
    private final IAzureStorageService azureStorageService;
    private final IDocumentHasTagService documentHasTagService;


    @Override
    public DocumentResponse uploadDocumentWithoutFolder(DocumentRequest documentRequest, MultipartFile file) {
        Document document = processValidDocument(documentRequest, file);
        return mapToDocumentResponse(document);
    }

    private Document processValidDocument(DocumentRequest documentRequest, MultipartFile file) {
        Document document = mapToDocument(documentRequest, file);
        document.setVersion(1);
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

    }

    @Override
    public DocumentResponse cloneDocumentById(Long documentId) {
        return null;
    }

    @Override
    public DocumentResponse updateDocumentById(Long documentId, DocumentRequest documentRequest) {
        return null;
    }
}
