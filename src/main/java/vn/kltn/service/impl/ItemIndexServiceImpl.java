package vn.kltn.service.impl;

import com.azure.storage.blob.models.BlobStorageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import vn.kltn.common.ItemType;
import vn.kltn.dto.response.ItemIndexResponse;
import vn.kltn.entity.Document;
import vn.kltn.entity.Item;
import vn.kltn.exception.CustomIOException;
import vn.kltn.exception.InsertIndexException;
import vn.kltn.index.ItemIndex;
import vn.kltn.map.ItemIndexMapper;
import vn.kltn.repository.elasticsearch.CustomItemIndexRepo;
import vn.kltn.repository.elasticsearch.ItemIndexRepo;
import vn.kltn.repository.util.FileUtil;
import vn.kltn.service.IAzureStorageService;
import vn.kltn.service.IItemIndexService;
import vn.kltn.util.RetryUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "DOCUMENT_INDEX_SERVICE")
public class ItemIndexServiceImpl implements IItemIndexService {
    private final ItemIndexRepo itemIndexRepo;
    private final ItemIndexMapper itemIndexMapper;
    private final CustomItemIndexRepo customItemIndexRepo;
    private final IAzureStorageService azureStorageService;
    private final DocumentCommonService documentCommonService;
    private final ItemGetterService itemGetterService;
    @Qualifier("taskExecutor")
    private final Executor taskExecutor;


    @Override
    @Async("taskExecutor")
    public void insertItem(Item item) {
        log.info("insert item Id: {}", item.getId());
        if (item.getItemType() == ItemType.FOLDER) {
            ItemIndex itemIndex = mapToItemIndex(item);
            itemIndexRepo.save(itemIndex);
            return;
        }
        Document doc = (Document) item;
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> RetryUtil.runWithRetry(() -> {
            String content = getContentFromBlob(doc.getCurrentVersion().getBlobName());
            ItemIndex itemIndex = mapToItemIndex(item);
            itemIndex.setContent(content);
            // Gửi content đến Elasticsearch hoặc xử lý tiếp ở đây
            itemIndexRepo.save(itemIndex);
            log.info("Inserted document Id: {}", item.getId());
        }, 3, 1000, IOException.class, BlobStorageException.class, TimeoutException.class), taskExecutor);

        try {
            future.join();
        } catch (CompletionException e) {
            log.error(" Error inserting document {}: {}", item.getId(), e.getCause().getMessage());
            throw new InsertIndexException("Có lỗi xảy ra khi insert dữ liệu");
        }
    }

    private String getContentFromBlob(String blobName) {
        try (InputStream inputStream = azureStorageService.downloadBlobInputStream(blobName)) {
            return FileUtil.extractTextByType(blobName, inputStream);
        } catch (IOException e) {
            log.error("Error downloading blob {}: {}", blobName, e.getMessage());
            throw new CustomIOException("Có lỗi xảy ra khi tải tài liệu lên Elasticsearch");
        }
    }


    @Override
    @Async("taskExecutor")
    public void deleteDocById(Long indexId) {
        log.info("delete index Id: {}", indexId);
        itemIndexRepo.deleteById(indexId.toString());
    }

    /**
     * đánh dấu field isDeleted = value
     *
     * @param indexId: id của document trong elasticsearch
     * @param value    : true/false
     */
    @Override
    @Async("taskExecutor")
    public void markDeleteItem(Long indexId, boolean value) {
        log.info("mark deleted indexId: {}", indexId);
        customItemIndexRepo.markDeletedByIndexId(indexId.toString(), value);
    }

    @Override
    public List<ItemIndexResponse> getItemShared(Set<Long> itemIds, String query, int page, int size) {
        log.info("getItemShared started");
        return customItemIndexRepo.getItemShared(itemIds, query, page, size);
    }

    @Override
    @Async("taskExecutor")
    public void deleteIndexByIdList(List<Long> indexIds) {
        log.info("delete indexIds: {}", indexIds);
        if (indexIds == null || indexIds.isEmpty()) return;
        customItemIndexRepo.deleteIndexByIdList(indexIds);
    }

    @Override
    @Async("taskExecutor")
    public void markDeleteItems(List<Long> indexIds, boolean value) {
        log.info("mark deleted indexIds: {}, value {}", indexIds, value);
        if (indexIds == null || indexIds.isEmpty()) return;
        List<String> indexIdsString = indexIds.stream().map(String::valueOf).toList();
        customItemIndexRepo.markDeleteItemsIndex(indexIdsString, value);
    }

    @Override
    @Async("taskExecutor")
    public CompletableFuture<List<ItemIndex>> insertAllItem(List<Item> items) {
        log.info(" insertAllDocAsync started");

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        List<ItemIndex> documentIndices = Collections.synchronizedList(new ArrayList<>());

        for (Item item : items) {
            if (item.getItemType() == ItemType.FOLDER) {
                ItemIndex itemIndex = mapToItemIndex(item);
                documentIndices.add(itemIndex);
                continue;
            }
            Document document = (Document) item;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try (InputStream inputStream = azureStorageService.downloadBlobInputStream(document.getCurrentVersion().getBlobName())) {

                    String content = FileUtil.extractTextByType(document.getType(), inputStream);
                    ItemIndex itemIndex = mapToItemIndex(document);
                    itemIndex.setContent(content);
                    documentIndices.add(itemIndex);
                    log.info(" Inserted document Id: {}", item.getId());

                } catch (IllegalArgumentException e) {
                    log.error(" Error downloading blob for document {}: {}", item.getId(), e.getMessage());
                } catch (Exception e) {
                    log.error(" Error processing document {}: {}", item.getId(), e.getMessage());
                }
            }, taskExecutor);

            futures.add(future);
        }

        // Chờ tất cả task xong
        CompletableFuture<Void> allDoneFuture = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        return allDoneFuture.thenApplyAsync(v -> {
            try {
                itemIndexRepo.saveAll(documentIndices);
                log.info(" All documents inserted into Elasticsearch successfully");
            } catch (Exception e) {
                log.error(" Error saving documents to Elasticsearch: {}", e.getMessage());
                throw new CustomIOException("Có lỗi xảy ra khi lưu tài liệu vào Elasticsearch");
            }

            return documentIndices;
        }, taskExecutor);
    }


    @Override
    @Async("taskExecutor")
    public void deleteAll(List<ItemIndex> itemIndices) {
        itemIndexRepo.deleteAll(itemIndices);
    }

    @Override
    @Async("taskExecutor")
    public void syncItem(Long docId) {
        log.info("sync item: {}", docId);
        Item item = itemGetterService.getItemByIdOrThrow(docId);
        if (item == null) return;
        customItemIndexRepo.updateItem(mapToItemIndex(item));
    }

    @Override
    @Async("taskExecutor")
    public void syncContentDocument(Long docId) {
        log.info("sync content documentId: {}", docId);
        Document document = documentCommonService.getDocumentById(docId);
        if (document == null) return;
        customItemIndexRepo.updateItem(mapDocContent(document));
    }

    @Override
    @Async("taskExecutor")
    public void syncItems(Set<Long> itemIds) {
        log.info("sync list items");
        List<Item> items = itemIds.stream().map(itemGetterService::getItemByIdOrThrow).toList();
        List<ItemIndex> indices = items.stream().map(this::mapToItemIndex).toList();
        customItemIndexRepo.bulkUpdate(indices);

    }

    private ItemIndex mapToItemIndex(Item item) {
        return itemIndexMapper.toIndex(item);
    }

    private ItemIndex mapDocContent(Document document) {
        ItemIndex itemIndex = itemIndexMapper.toIndex(document);
        try (InputStream inputStream = azureStorageService.downloadBlobInputStream(document.getCurrentVersion().getBlobName())) {
            String content = FileUtil.extractTextByType(document.getType(), inputStream);
            itemIndex.setContent(content);
        } catch (IOException e) {
            log.error("Error downloading blob for document {}: {}", document.getId(), e.getMessage());
        }
        return itemIndex;
    }


}
