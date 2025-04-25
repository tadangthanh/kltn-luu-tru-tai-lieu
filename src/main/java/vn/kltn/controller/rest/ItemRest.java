package vn.kltn.controller.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.kltn.dto.response.ItemResponse;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.dto.response.ResponseData;
import vn.kltn.entity.Item;
import vn.kltn.service.IItemCommonService;
import vn.kltn.service.IItemService;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/items")
public class ItemRest {
    private final IItemService itemService;

    @GetMapping
    public ResponseData<PageResponse<List<ItemResponse>>> search(Pageable pageable, @RequestParam(required = false, value = "items") String[] items) {
        return new ResponseData<>(200, "Thành công", itemService.searchByCurrentUser(pageable, items));
    }

    @GetMapping("/emails")
    public ResponseData<PageResponse<List<String>>> getEmailsSharedWithMe(Pageable pageable) {
        return new ResponseData<>(200, "Thành công", itemService.getEmailsSharedWithMe(pageable));
    }
}
