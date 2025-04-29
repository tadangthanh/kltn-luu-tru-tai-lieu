package vn.kltn.service;

import org.springframework.data.domain.Pageable;
import vn.kltn.dto.request.ItemRequest;
import vn.kltn.dto.response.ItemResponse;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.entity.Item;

import java.util.List;

public interface IItemService {
    PageResponse<List<ItemResponse>> searchByCurrentUser(Pageable pageable, String[] items);

    Item getItemByIdOrThrow(Long itemId);

    void softDeleteItemById(Long itemId);

    PageResponse<List<String>> getEmailsSharedWithMe(Pageable pageable, String keyword);

    ItemResponse updateItem(Long itemId, ItemRequest itemRequest);
}
