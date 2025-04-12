package vn.kltn.repository.elasticsearch;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import vn.kltn.index.DocumentIndex;

import java.util.List;

public interface DocumentIndexRepo extends ElasticsearchRepository<DocumentIndex, String> {
    void deleteByDocumentId(Long documentId);

    void deleteAllByDocumentIdIn(List<Long> documentIds);
}
