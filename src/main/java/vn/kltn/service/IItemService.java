package vn.kltn.service;

import org.springframework.data.domain.Pageable;
import vn.kltn.dto.request.ItemRequest;
import vn.kltn.dto.response.ItemResponse;
import vn.kltn.dto.response.PageItemResponse;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.entity.Item;

import java.util.List;

public interface IItemService {
    PageItemResponse<List<ItemResponse>> getMyItems(Pageable pageable, String[] filters);

    PageItemResponse<List<ItemResponse>> getItemsSharedWithMe(Pageable pageable, String[] items);

    Item getItemByIdOrThrow(Long itemId);

    void softDeleteItemById(Long itemId);

    PageResponse<List<String>> getEmailsSharedWithMe(Pageable pageable, String keyword);

    ItemResponse updateItem(Long itemId, ItemRequest itemRequest);

    PageResponse<List<ItemResponse>> getItemsMarkDelete(Pageable pageable);

    void cleanUpTrash(); // xóa vĩnh viễn các item đã xóa trong thùng rác

    ItemResponse restoreItemById(Long itemId); // phục hồi item đã xóa

    void deleteItemForever(Long itemId); // xóa vĩnh viễn item đã xóa trong thùng rác

    void showItem(Long itemId);
}
