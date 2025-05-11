package vn.kltn.util;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.kltn.dto.BreadcrumbDto;
import vn.kltn.dto.response.PageItemResponse;

import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

public class PaginationItemUtils {
    public static <T, R> PageItemResponse<List<R>> convertToPageItemResponse(
            Page<T> entityPage,
            Pageable pageable,
            Function<T, R> mapper, List<BreadcrumbDto> breadcrumbs) {

        List<R> responseList = entityPage.stream()
                .map(mapper)
                .collect(toList());

        return PageItemResponse.<List<R>>builder()
                .items(responseList)
                .totalItems(entityPage.getTotalElements())
                .totalPage(entityPage.getTotalPages())
                .hasNext(entityPage.hasNext())
                .pageNo(pageable.getPageNumber())
                .pageSize(pageable.getPageSize())
                .breadcrumbs(breadcrumbs)
                .build();
    }
}
