package vn.kltn.service;

import vn.kltn.dto.response.ItemSearchResponse;
import vn.kltn.entity.Item;
import vn.kltn.index.ItemIndex;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface IItemIndexService {
    void insertItem(Item item);

    void deleteDocById(Long indexId);

    void markDeleteItem(Long indexId, boolean value);

    List<ItemSearchResponse> getItemShared(Set<Long> itemIds, String query, int page, int size);

    void deleteIndexByIdList(List<Long> indexIds);

    void markDeleteItems(List<Long> indexIds, boolean value);

    CompletableFuture<List<ItemIndex>> insertAllItem(List<Item> items);

    void deleteAll(List<ItemIndex> itemIndices);

    void syncItem(Long docId);

    void syncContentDocument(Long docId);

    void syncItems(Set<Long> documentIds);

}
