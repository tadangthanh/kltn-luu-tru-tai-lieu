package vn.kltn.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.kltn.dto.FileBuffer;
import vn.kltn.dto.request.DocumentRequest;
import vn.kltn.dto.response.DocumentDataResponse;
import vn.kltn.dto.response.DocumentResponse;
import vn.kltn.dto.response.ItemResponse;
import vn.kltn.entity.Document;
import vn.kltn.entity.User;
import vn.kltn.exception.CustomIOException;
import vn.kltn.exception.InvalidDataException;
import vn.kltn.map.DocumentMapper;
import vn.kltn.repository.DocumentRepo;
import vn.kltn.service.IAuthenticationService;
import vn.kltn.service.IAzureStorageService;
import vn.kltn.service.IDocumentMapperService;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j(topic = "DOCUMENT_MAPPER_SERVICE")
@RequiredArgsConstructor
public class DocumentMapperServiceImpl implements IDocumentMapperService {
    private final DocumentMapper documentMapper;
    private final IAuthenticationService authenticationService;
    private final DocumentRepo documentRepo;
    private final IAzureStorageService azureStorageService;
    @Override
    public DocumentResponse mapToDocumentResponse(Document document) {
        return documentMapper.toDocumentResponse(document);
    }


    @Override
    public void updateDocument(Document docExists, DocumentRequest documentRequest) {
        documentMapper.updateDocument(docExists,documentRequest);
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
            Document document = this.mapFileBufferToDocument(buffer);
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
