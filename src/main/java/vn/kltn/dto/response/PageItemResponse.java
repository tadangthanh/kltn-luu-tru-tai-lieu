package vn.kltn.dto.response;

import lombok.Builder;
import lombok.Getter;
import vn.kltn.dto.BreadcrumbDto;

import java.io.Serializable;
import java.util.List;

@Getter
@Builder
public class PageItemResponse<T> implements Serializable {
    private int pageNo;
    private int pageSize;
    private int totalPage;
    private long totalItems;
    private boolean hasNext;
    private T items;
    private List<BreadcrumbDto> breadcrumbs;
}
