package vn.kltn.repository.elasticsearch;

import vn.kltn.dto.response.ItemSearchResponse;
import vn.kltn.index.ItemIndex;

import java.util.List;
import java.util.Set;

public interface CustomItemIndexRepo {
    void markDeletedByIndexId(String indexId, boolean value);

    void updateItem(ItemIndex itemUpdated);

    void deleteIndexByIdList(List<Long> indexIds);

    void markDeleteItemsIndex(List<String> indexIds, boolean value);

    List<ItemSearchResponse> getItemShared(Set<Long> itemIds, String query, int page, int size);

    void bulkUpdate(List<ItemIndex> indices);
}
