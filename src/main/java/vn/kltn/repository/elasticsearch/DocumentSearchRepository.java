package vn.kltn.repository.elasticsearch;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import vn.kltn.index.DocumentSearchEntity;

import java.util.List;

public interface DocumentSearchRepository extends ElasticsearchRepository<DocumentSearchEntity, String> {
    List<DocumentSearchEntity> findByDescriptionContainingOrContentContainingOrName(String description, String content,String name);

    List<DocumentSearchEntity> findByTags(String tag);
}
