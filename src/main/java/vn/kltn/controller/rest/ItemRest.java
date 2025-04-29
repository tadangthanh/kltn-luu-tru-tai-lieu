package vn.kltn.controller.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.kltn.dto.request.ItemRequest;
import vn.kltn.dto.response.ItemResponse;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.dto.response.ResponseData;
import vn.kltn.service.IItemService;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/items")
@Validated
public class ItemRest {
    private final IItemService itemService;

    @GetMapping
    public ResponseData<PageResponse<List<ItemResponse>>> search(Pageable pageable, @RequestParam(required = false, value = "items") String[] items) {
        return new ResponseData<>(200, "Thành công", itemService.searchByCurrentUser(pageable, items));
    }

    @GetMapping("/emails")
    public ResponseData<PageResponse<List<String>>> getEmailsSharedWithMe(Pageable pageable, @RequestParam(required = false, value = "keyword") String keyword) {
        return new ResponseData<>(200, "Thành công", itemService.getEmailsSharedWithMe(pageable, keyword));
    }

    @PutMapping("/{itemId}")
    public ResponseData<ItemResponse> updateItem(@PathVariable Long itemId, @Valid @RequestBody ItemRequest itemRequest) {
        return new ResponseData<>(200, "Thành công", itemService.updateItem(itemId, itemRequest));
    }

    @DeleteMapping("/{itemId}")
    public ResponseData<Void> deleteItem(@PathVariable Long itemId) {
        itemService.softDeleteItemById(itemId);
        return new ResponseData<>(200, "Xóa thành công");
    }
}
