package vn.kltn.util;

import org.springframework.data.domain.Pageable;
import vn.kltn.dto.response.PageResponse;

import java.util.List;

public class PaginationIndexUtils {
    public static <T> PageResponse<List<T>> convertToPageResponse(
            List<T> listResponse, long totalItems, int totalPage, Pageable pageable, boolean hasNext) {

        return PageResponse.<List<T>>builder()
                .items(listResponse)
                .totalItems(totalItems)
                .totalPage(totalPage)
                .hasNext(hasNext)
                .pageNo(pageable.getPageNumber())
                .pageSize(pageable.getPageSize())
                .build();
    }
}
