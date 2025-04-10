package vn.kltn.repository.elasticsearch;

import java.util.List;

public interface CustomDocumentSegmentRepo {
    void markDeletedByDocumentId(Long documentId);

    void markDeleteByDocumentIds(List<Long> documentIds);
}
