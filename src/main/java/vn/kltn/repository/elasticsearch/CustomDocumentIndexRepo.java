package vn.kltn.repository.elasticsearch;

import vn.kltn.dto.response.DocumentIndexResponse;
import vn.kltn.index.DocumentIndex;

import java.util.List;
import java.util.Set;

public interface CustomDocumentIndexRepo {
    void markDeletedByIndexId(String indexId, boolean value);

    void updateDocument(DocumentIndex documentUpdated);

    void deleteIndexByIdList(List<Long> indexIds);

    void markDeleteDocumentsIndex(List<String> indexIds, boolean value);

    List<DocumentIndexResponse> getDocumentShared(Set<Long> documentIds, String query, int page, int size);

    void bulkUpdate(List<DocumentIndex> indices);
}
