package vn.kltn.service;

import vn.kltn.dto.response.DocumentResponse;
import vn.kltn.entity.Document;

public interface IDocumentMapperService {
    DocumentResponse mapToDocumentResponse(Document document);
}
