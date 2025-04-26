package vn.kltn.service;

import org.springframework.data.domain.Pageable;
import vn.kltn.dto.response.ItemResponse;
import vn.kltn.dto.response.PageResponse;

import java.util.List;

public interface IItemService {
    PageResponse<List<ItemResponse>> searchByCurrentUser(Pageable pageable, String[] items);

    PageResponse<List<String>> getEmailsSharedWithMe(Pageable pageable, String keyword);
}
