package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.kltn.entity.Document;
import vn.kltn.entity.Item;
import vn.kltn.index.ItemIndex;
import vn.kltn.repository.DocumentRepo;
import vn.kltn.repository.ItemRepo;
import vn.kltn.service.IAzureStorageService;
import vn.kltn.service.IItemIndexService;

import java.util.List;
import java.util.concurrent.CancellationException;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j(topic = "UPLOAD_CLEANUP_SERVICE")
public class UploadCleanupService {
    private final DocumentRepo documentRepo;
    private final IItemIndexService documentIndexService;
    private final IAzureStorageService azureStorageService;
    private final ItemRepo itemRepo;


    public void cleanupUpload(List<Item> items, List<ItemIndex> documentIndices, List<String> blobNames) {
        log.info("Bắt đầu dọn dẹp tài liệu bị hủy");
        try {
            // Xoá document trong MySQL
            deleteItems(items);
            // Xoá document trong Elasticsearch
            deleteDocumentsIndex(documentIndices);
            // Xoá blob trên Azure
            azureStorageService.deleteBLobs(blobNames);
        } catch (Exception e) {
            log.error(" Lỗi khi dọn dẹp sau khi huỷ upload: {}", e.getMessage());
            throw new CancellationException("Lỗi khi huỷ upload");
        }
    }

    private void deleteItems(List<Item> items) {
        if (items != null && !items.isEmpty()) {
            itemRepo.deleteAll(items);
            log.info("Đã xoá {} tài liệu trong MySQL", items.size());
        }
    }

    private void deleteDocumentsIndex(List<ItemIndex> documentIndices) {
        if (documentIndices != null && !documentIndices.isEmpty()) {
            documentIndexService.deleteAll(documentIndices);
            log.info("Đã xoá {} tài liệu trong Elasticsearch", documentIndices.size());
        }
    }
}
