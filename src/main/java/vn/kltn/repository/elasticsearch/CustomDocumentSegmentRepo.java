package vn.kltn.repository.elasticsearch;

import vn.kltn.index.DocumentSegmentEntity;

import java.util.List;
import java.util.Set;

public interface CustomDocumentSegmentRepo {
    void markDeletedByDocumentId(Long documentId, boolean value);

    void updateDocument(DocumentSegmentEntity documentUpdated);

    void markDeletedByDocumentIds(List<Long> documentIds, boolean value);

    List<DocumentSegmentEntity> getDocumentByMe(Set<Long> listDocumentSharedWith, String query, int page, int size);
}
