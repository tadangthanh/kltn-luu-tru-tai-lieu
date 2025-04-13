package vn.kltn.service;

import vn.kltn.dto.response.DocumentIndexResponse;
import vn.kltn.entity.Document;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

public interface IDocumentIndexService {
    void insertDoc(Document document, InputStream inputStream);

    void deleteIndex(String indexId);

    void markDeleteDocument(String indexId, boolean value);

    List<DocumentIndexResponse> getDocumentByMe(Set<Long> listDocumentSharedWith, String query, int page, int size);

    void updateDocument(Document document);

    void deleteIndexByIdList(List<Long> indexIds);

    void markDeleteDocumentsIndex(List<String> indexIds, boolean value);

    void insertAllDoc(List<Document> documents);
}
