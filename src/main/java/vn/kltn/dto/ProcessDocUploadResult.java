package vn.kltn.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import vn.kltn.dto.response.ItemResponse;

import java.util.List;

@Getter
@AllArgsConstructor
public class ProcessDocUploadResult {
    private final boolean cancelled;
    private final List<ItemResponse> items;
}
