package vn.kltn.service;

import vn.kltn.dto.request.TagRequest;
import vn.kltn.entity.Document;
import vn.kltn.entity.File;

public interface IDocumentHasTagService {
    void addDocumentToTag(Document document, TagRequest[] tags);

    void deleteByDocumentId(Long documentId);
}
