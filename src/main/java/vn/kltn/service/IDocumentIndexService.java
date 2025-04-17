package vn.kltn.service;

import vn.kltn.dto.response.DocumentIndexResponse;
import vn.kltn.entity.Document;
import vn.kltn.index.DocumentIndex;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface IDocumentIndexService {
    void insertDoc(Document document);

    void deleteIndex(String indexId);

    void markDeleteDocument(String indexId, boolean value);

    List<DocumentIndexResponse> getDocumentByMe(Set<Long> listDocumentSharedWith, String query, int page, int size);


    void deleteIndexByIdList(List<Long> indexIds);

    void markDeleteDocumentsIndex(List<String> indexIds, boolean value);

    CompletableFuture<List<DocumentIndex>> insertAllDoc(List<Document> documents);

    void deleteAll(List<DocumentIndex> documentIndices);

    void syncDocument(Long docId);

    void syncDocuments(Set<Long> documentIds);

}
