package vn.kltn.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.kltn.dto.FileBuffer;
import vn.kltn.dto.request.DocumentRequest;
import vn.kltn.dto.response.DocumentDataResponse;
import vn.kltn.dto.response.DocumentResponse;
import vn.kltn.entity.Document;
import vn.kltn.entity.User;
import vn.kltn.exception.CustomIOException;
import vn.kltn.map.DocumentMapper;
import vn.kltn.service.IAuthenticationService;
import vn.kltn.service.IAzureStorageService;
import vn.kltn.service.IDocumentMapperService;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j(topic = "DOCUMENT_MAPPER_SERVICE")
@RequiredArgsConstructor
public class DocumentMapperServiceImpl implements IDocumentMapperService {
    private final DocumentMapper documentMapper;
    private final IAuthenticationService authenticationService;
    private final IAzureStorageService azureStorageService;

    @Override
    public DocumentResponse mapToDocumentResponse(Document document) {
        return documentMapper.toDocumentResponse(document);
    }


    @Override
    public void updateDocument(Document docExists, DocumentRequest documentRequest) {
        documentMapper.updateDocument(docExists, documentRequest);
    }

    @Override
    public List<DocumentResponse> mapToDocumentResponseList(List<Document> documents) {
        return documentMapper.toDocumentResponseList(documents);
    }

    @Override
    public Document copyDocument(Document document) {
        return documentMapper.copyDocument(document);
    }

    @Override
    public Document mapFileBufferToDocument(FileBuffer buffer) {
        return documentMapper.mapFileBufferToDocument(buffer);
    }

    @Override
    public List<Document> mapFilesBufferToListDocument(List<FileBuffer> bufferList) {
        List<Document> documents = new ArrayList<>();
        User uploader = authenticationService.getCurrentUser();
        // Duyệt từng file
        for (FileBuffer buffer : bufferList) {
            Document document = this.mapFileBufferToDocument(buffer);
            document.setOwner(uploader);
            documents.add(document);
        }
        return documents;
    }

    @Override
    public DocumentDataResponse mapDocToDocDataResponse(Document document) {
        String blobName = document.getBlobName();
        try (InputStream inputStream = azureStorageService.downloadBlobInputStream(blobName)) {
            return DocumentDataResponse.builder()
                    .data(inputStream.readAllBytes())
                    .name(document.getName() + document.getBlobName()
                            .substring(document.getBlobName()
                                    .lastIndexOf('.')))
                    .type(document.getType()).documentId(document.getId()).build();
        } catch (IOException e) {
            log.error("Error reading file from Azure Storage: {}", e.getMessage());
            throw new CustomIOException("Lỗi đọc dữ liệu từ file");
        }
    }

    @Override
    public void mapBlobNamesToDocuments(List<Document> documents, List<String> blobNames) {
        for (int i = 0; i < documents.size(); i++) {
            documents.get(i).setBlobName(blobNames.get(i));
        }
    }
}
