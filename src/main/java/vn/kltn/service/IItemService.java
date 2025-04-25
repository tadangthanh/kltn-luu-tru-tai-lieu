package vn.kltn.service;

import org.springframework.data.domain.Pageable;
import vn.kltn.dto.response.ItemResponse;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.entity.Item;

import java.util.List;

// T là kiểu dữ liệu của entity resource(document or folder), R là kiểu dữ liệu của response (DocumentResponse or FolderResponse)
public interface IItemService<T extends Item, R extends ItemResponse> {

    void deleteItemById(Long itemId);

    R restoreItemById(Long itemId);

    R getItemById(Long itemId);

    T getItemByIdOrThrow(Long itemId);

    void hardDeleteItemById(Long itemId);

    R moveItemToFolder(Long itemId, Long folderId);

    PageResponse<List<R>> searchByCurrentUser(Pageable pageable, String[] items);
}
