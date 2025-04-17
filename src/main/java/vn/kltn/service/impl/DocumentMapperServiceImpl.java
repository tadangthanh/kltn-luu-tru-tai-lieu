package vn.kltn.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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
}
