package vn.kltn.service;

import vn.kltn.dto.FileBuffer;
import vn.kltn.dto.request.DocumentRequest;
import vn.kltn.dto.response.DocumentResponse;
import vn.kltn.entity.Document;

public interface IDocumentMapperService {
    DocumentResponse mapToDocumentResponse(Document document);

    void updateDocument(Document docExists, DocumentRequest documentRequest);

    Document copyDocument(Document document);

    Document mapFileBufferToDocument(FileBuffer buffer);
}
