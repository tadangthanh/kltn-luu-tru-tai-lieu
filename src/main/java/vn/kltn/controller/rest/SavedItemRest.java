package vn.kltn.controller.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import vn.kltn.dto.response.ItemResponse;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.dto.response.ResponseData;
import vn.kltn.service.ISavedItemService;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/saved-items")
public class SavedItemRest {
    private final ISavedItemService savedItemService;

    @PostMapping("/add/{itemId}")
    public ResponseData<Void> addSavedItem(@PathVariable Long itemId) {
        savedItemService.addSavedItem(itemId);
        return new ResponseData<>(200, "Saved item added successfully", null);
    }

    @DeleteMapping("/remove/{savedItemId}")
    public ResponseData<Void> removeSavedItem(@PathVariable Long savedItemId) {
        savedItemService.removeSavedItem(savedItemId);
        return new ResponseData<>(200, "Saved item removed successfully", null);
    }

    @GetMapping
    public ResponseData<PageResponse<List<ItemResponse>>> getSavedItems(Pageable pageable) {
        return new ResponseData<>(200, "Saved items retrieved successfully",
                savedItemService.getSavedItems(pageable));
    }

}
