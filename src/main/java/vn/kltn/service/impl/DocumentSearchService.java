package vn.kltn.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.kltn.index.DocumentSearchEntity;
import vn.kltn.repository.elasticsearch.DocumentSearchRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "DOCUMENT_SEARCH_SERVICE")
public class DocumentSearchService {
    private final DocumentSearchRepository repository;

    public List<DocumentSearchEntity> search(String keyword) {
        return repository.findByDescriptionContainingOrContentContainingOrName(keyword, keyword, keyword);
    }

    public List<DocumentSearchEntity> searchByTag(String tag) {
        return repository.findByTags(tag);
    }

    public void indexDocument(DocumentSearchEntity doc) {
        repository.save(doc);
    }
}
