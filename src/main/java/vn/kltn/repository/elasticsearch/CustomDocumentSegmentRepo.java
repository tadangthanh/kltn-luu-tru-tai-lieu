package vn.kltn.repository.elasticsearch;

import vn.kltn.index.DocumentSegmentEntity;

import java.util.List;

public interface CustomDocumentSegmentRepo {
    void markDeletedByDocumentId(Long documentId, boolean value);

    void updateDocument(DocumentSegmentEntity documentUpdated);

    void markDeleteByDocumentIds(List<Long> documentIds);
}
