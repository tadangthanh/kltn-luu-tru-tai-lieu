package vn.kltn.service;

import vn.kltn.dto.FileBuffer;
import vn.kltn.dto.request.DocumentRequest;
import vn.kltn.dto.response.DocumentDataResponse;
import vn.kltn.dto.response.DocumentResponse;
import vn.kltn.entity.Document;

import java.util.List;

public interface IDocumentMapperService {
    DocumentResponse mapToDocumentResponse(Document document);

    void updateDocument(Document docExists, DocumentRequest documentRequest);

    List<DocumentResponse> mapToDocumentResponseList(List<Document> documents);

    Document copyDocument(Document document);

    Document mapFileBufferToDocument(FileBuffer buffer);

    List<Document> mapFilesBufferToListDocument(List<FileBuffer> bufferList);

    DocumentDataResponse mapDocToDocDataResponse(Document document);

    void mapBlobNamesToDocuments(List<Document> documents, List<String> blobNames);
}
