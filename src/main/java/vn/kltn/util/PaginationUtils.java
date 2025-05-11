package vn.kltn.util;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.kltn.dto.response.PageResponse;

import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

public class PaginationUtils {
    public static <T, R> PageResponse<List<R>> convertToPageResponse(
            Page<T> entityPage,
            Pageable pageable,
            Function<T, R> mapper) {

        List<R> responseList = entityPage.stream()
                .map(mapper)
                .collect(toList());

        return PageResponse.<List<R>>builder()
                .items(responseList)
                .totalItems(entityPage.getTotalElements())
                .totalPage(entityPage.getTotalPages())
                .hasNext(entityPage.hasNext())
                .pageNo(pageable.getPageNumber())
                .pageSize(pageable.getPageSize())
                .build();
    }
}
