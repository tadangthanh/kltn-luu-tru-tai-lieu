package vn.kltn.service;

import vn.kltn.entity.Document;
import vn.kltn.index.DocumentSegmentEntity;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

public interface IDocumentIndexService {
    void indexDocument(Document document, InputStream inputStream);

    void deleteIndexByDocumentId(Long documentId);

    void deleteIndexByListDocumentId(List<Long> documentIds);

    void markDeleteDocument(Long documentId, boolean value);

    void markDeleteDocuments(List<Long> documentIds, boolean value);

    List<DocumentSegmentEntity> getDocumentByMe(Set<Long> listDocumentSharedWith, String query, int page, int size);

    void updateDocument(Document document);
}
