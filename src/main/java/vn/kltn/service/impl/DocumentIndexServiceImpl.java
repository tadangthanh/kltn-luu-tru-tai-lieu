package vn.kltn.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import vn.kltn.dto.response.DocumentIndexResponse;
import vn.kltn.entity.Document;
import vn.kltn.entity.Tag;
import vn.kltn.exception.CustomIOException;
import vn.kltn.index.DocumentIndex;
import vn.kltn.map.DocumentIndexMapper;
import vn.kltn.repository.elasticsearch.CustomDocumentIndexRepo;
import vn.kltn.repository.elasticsearch.DocumentIndexRepo;
import vn.kltn.repository.util.FileUtil;
import vn.kltn.service.IAzureStorageService;
import vn.kltn.service.IDocumentHasTagService;
import vn.kltn.service.IDocumentIndexService;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "DOCUMENT_INDEX_SERVICE")
public class DocumentIndexServiceImpl implements IDocumentIndexService {
    private final DocumentIndexRepo documentIndexRepo;
    private final DocumentIndexMapper documentIndexMapper;
    private final IDocumentHasTagService documentHasTagService;
    private final DocumentPermissionCommonService documentPermissionCommonService;
    private final CustomDocumentIndexRepo customDocumentIndexRepo;
    private final IAzureStorageService azureStorageService;
    @Qualifier("taskExecutor")
    private final Executor taskExecutor;
    @Override
    @Async
    public void insertDoc(Document document, InputStream inputStream) {
        log.info("insert document Id: {}", document.getId());
        String content = FileUtil.extractTextByType(document.getType(), inputStream);
        DocumentIndex documentIndex = mapDocumentIndex(document);
        documentIndex.setContent(content);
        documentIndexRepo.save(documentIndex);
    }

    @Override
    @Async
    public void deleteIndex(String indexId) {
        log.info("delete index Id: {}", indexId);
        documentIndexRepo.deleteById(indexId);
    }

    /**
     * đánh dấu field isDeleted = value
     *
     * @param indexId: id của document trong elasticsearch
     * @param value    : true/false
     */
    @Override
    @Async
    public void markDeleteDocument(String indexId, boolean value) {
        log.info("mark deleted indexId: {}", indexId);
        customDocumentIndexRepo.markDeletedByIndexId(indexId, value);
    }

    @Override
    public List<DocumentIndexResponse> getDocumentByMe(Set<Long> listDocumentSharedWith, String query, int page, int size) {
        return customDocumentIndexRepo.getDocumentByMe(listDocumentSharedWith, query, page, size);
    }

    @Override
    @Async
    public void updateDocument(Document document) {
        log.info("update documentId: {}", document.getId());
        customDocumentIndexRepo.updateDocument(mapDocumentIndex(document));
    }

    @Override
    public void deleteIndexByIdList(List<Long> indexIds) {
        customDocumentIndexRepo.deleteIndexByIdList(indexIds);
    }

    @Override
    public void markDeleteDocumentsIndex(List<String> indexIds, boolean value) {
        customDocumentIndexRepo.markDeleteDocumentsIndex(indexIds, value);
    }

    @Override
    @Async("taskExecutor") // Optional: gọi từ nơi khác để async toàn bộ
    public void insertAllDoc(List<Document> documents) {
        log.info("insert all document");

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        List<DocumentIndex> documentIndices = Collections.synchronizedList(new ArrayList<>());

        for (Document document : documents) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try (InputStream inputStream = azureStorageService.downloadBlobInputStream(document.getBlobName())) {

                    String content = FileUtil.extractTextByType(document.getType(), inputStream);
                    DocumentIndex documentIndex = mapDocumentIndex(document);
                    documentIndex.setContent(content);
                    documentIndices.add(documentIndex);

                    log.info("Inserted document Id: {}", document.getId());

                } catch (IllegalArgumentException e) {
                    log.error(" Error downloading blob: {}", e.getMessage());
                } catch (Exception e) {
                    log.error(" Error inserting document: {}", e.getMessage());
                }
            }, taskExecutor);

            futures.add(future);
        }

        // Chờ tất cả task xong
        CompletableFuture<Void> allDoneFuture = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        allDoneFuture.join();

        // Lưu vào Elasticsearch
        try {
            documentIndexRepo.saveAll(documentIndices);
            log.info("✅ All documents inserted successfully");
        } catch (Exception e) {
            log.error("❌ Error saving all documents: {}", e.getMessage());
            throw new CustomIOException("Error saving all documents");
        }
    }


    private List<String> getTagsByDocumentId(Long documentId) {
        return documentHasTagService.getTagsByDocumentId(documentId).stream().map(Tag::getName).toList();
    }

    private DocumentIndex mapDocumentIndex(Document document) {
        DocumentIndex documentIndex = documentIndexMapper.toIndex(document);
        List<String> tagsList = getTagsByDocumentId(document.getId());
        List<Long> sharedWith = getUserIdsByDocumentShared(document.getId());
        documentIndex.setTags(tagsList);
        documentIndex.setSharedWith(sharedWith);
        return documentIndex;
    }

    private List<Long> getUserIdsByDocumentShared(Long documentId) {
        return documentPermissionCommonService.getUserIdsByDocumentShared(documentId).stream().toList();
    }

}
