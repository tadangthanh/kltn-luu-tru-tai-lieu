package vn.kltn.repository.elasticsearch;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import vn.kltn.index.DocumentSegmentEntity;

public interface DocumentSegmentRepo extends ElasticsearchRepository<DocumentSegmentEntity,String> {
}
