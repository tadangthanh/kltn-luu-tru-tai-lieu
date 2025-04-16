package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.kltn.entity.Document;
import vn.kltn.index.DocumentIndex;
import vn.kltn.repository.DocumentRepo;
import vn.kltn.repository.elasticsearch.DocumentIndexRepo;
import vn.kltn.service.IAzureStorageService;
import vn.kltn.service.IDocumentIndexService;

import java.util.List;
import java.util.concurrent.CancellationException;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j(topic = "UPLOAD_CLEANUP_SERVICE")
public class UploadCleanupService {
    private final DocumentRepo documentRepo;
    private final IDocumentIndexService documentIndexService;
    private final IAzureStorageService azureStorageService;


    public void cleanupUpload(List<Document> documents, List<DocumentIndex> documentIndices, List<String> blobNames) {
        log.info("Bắt đầu dọn dẹp tài liệu bị hủy");
        try {
            // Xoá document trong MySQL
            deleteDocuments(documents);
            // Xoá document trong Elasticsearch
            deleteDocumentsIndex(documentIndices);
            // Xoá blob trên Azure
            azureStorageService.deleteBLobs(blobNames);
        } catch (Exception e) {
            log.error(" Lỗi khi dọn dẹp sau khi huỷ upload: {}", e.getMessage());
            throw new CancellationException("Lỗi khi huỷ upload");
        }
    }

    private void deleteDocuments(List<Document> documents) {
        if (documents != null && !documents.isEmpty()) {
            documentRepo.deleteAll(documents);
            log.info("Đã xoá {} tài liệu trong MySQL", documents.size());
        }
    }

    private void deleteDocumentsIndex(List<DocumentIndex> documentIndices) {
        if (documentIndices != null && !documentIndices.isEmpty()) {
            documentIndexService.deleteAll(documentIndices);
            log.info("Đã xoá {} tài liệu trong Elasticsearch", documentIndices.size());
        }
    }
}
