package vn.kltn.service;

import vn.kltn.entity.Document;

import java.io.InputStream;
import java.util.List;

public interface IDocumentIndexService {
    void indexDocument(Document document, InputStream inputStream);

    void deleteIndexByDocumentId(Long documentId);

    void markDeleteByDocumentIds(List<Long> documentIds);

    void deleteIndexByDocumentIds(List<Long> documentIds);
    void markDeleteDocument(Long documentId);
}
