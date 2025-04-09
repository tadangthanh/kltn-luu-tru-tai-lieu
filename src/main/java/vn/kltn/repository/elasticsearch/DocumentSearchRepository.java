package vn.kltn.repository.elasticsearch;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import vn.kltn.index.DocumentSearchEntity;

import java.util.List;

public interface DocumentSearchRepository extends ElasticsearchRepository<DocumentSearchEntity, String> {
    List<DocumentSearchEntity> findByTitleContainingOrContentContaining(String title, String content);

    List<DocumentSearchEntity> findByTags(String tag);
}
