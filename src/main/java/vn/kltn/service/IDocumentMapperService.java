package vn.kltn.service;

import vn.kltn.dto.FileBuffer;
import vn.kltn.dto.request.DocumentRequest;
import vn.kltn.dto.response.DocumentResponse;
import vn.kltn.entity.Document;

import java.util.List;

public interface IDocumentMapperService {
    DocumentResponse mapToDocumentResponse(Document document);

    void updateDocument(Document docExists, DocumentRequest documentRequest);

    Document copyDocument(Document document);

    Document mapFileBufferToDocument(FileBuffer buffer);

    List<Document> mapFilesBufferToListDocument(List<FileBuffer> bufferList);

    void mapBlobNamesToDocuments(List<Document> documents, List<String> blobNames);
}
