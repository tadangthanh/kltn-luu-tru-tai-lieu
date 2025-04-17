package vn.kltn.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.kltn.dto.FileBuffer;
import vn.kltn.dto.request.DocumentRequest;
import vn.kltn.dto.response.DocumentResponse;
import vn.kltn.entity.Document;
import vn.kltn.map.DocumentMapper;
import vn.kltn.service.IDocumentMapperService;
@Service
@Slf4j(topic = "DOCUMENT_MAPPER_SERVICE")
@RequiredArgsConstructor
public class DocumentMapperServiceImpl implements IDocumentMapperService {
    private final DocumentMapper documentMapper;

    @Override
    public DocumentResponse mapToDocumentResponse(Document document) {
        return documentMapper.toDocumentResponse(document);
    }

    @Override
    public void updateDocument(Document docExists, DocumentRequest documentRequest) {
        documentMapper.updateDocument(docExists,documentRequest);
    }

    @Override
    public Document copyDocument(Document document) {
        return documentMapper.copyDocument(document);
    }

    @Override
    public Document mapFileBufferToDocument(FileBuffer buffer) {
        return documentMapper.mapFileBufferToDocument(buffer);
    }
}
