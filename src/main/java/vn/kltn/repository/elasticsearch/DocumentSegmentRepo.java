package vn.kltn.repository.elasticsearch;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import vn.kltn.index.DocumentSegmentEntity;

import java.util.List;

public interface DocumentSegmentRepo extends ElasticsearchRepository<DocumentSegmentEntity, String> {
    void deleteByDocumentId(Long documentId);

    void deleteAllByDocumentIdIn(List<Long> documentIds);
}
