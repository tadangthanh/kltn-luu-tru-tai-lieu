package vn.kltn.service;

import org.springframework.data.domain.Pageable;
import vn.kltn.dto.response.ItemResponse;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.entity.SavedItem;

import java.util.List;

public interface ISavedItemService {
    SavedItem addSavedItem(Long itemId);

    void removeByItemId(Long itemId);

    PageResponse<List<ItemResponse>> getSavedItems(Pageable pageable);

}
