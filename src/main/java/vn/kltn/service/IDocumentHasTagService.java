package vn.kltn.service;

import vn.kltn.dto.request.TagRequest;
import vn.kltn.entity.Document;
import vn.kltn.entity.File;
import vn.kltn.entity.Tag;

import java.util.Set;

public interface IDocumentHasTagService {
    void addDocumentToTag(Document document, TagRequest[] tags);
    void addDocumentToTag(Document document, Set<Tag> tags);

    void deleteByDocumentId(Long documentId);

    Set<Tag> getTagsByDocumentId(Long id);
}
