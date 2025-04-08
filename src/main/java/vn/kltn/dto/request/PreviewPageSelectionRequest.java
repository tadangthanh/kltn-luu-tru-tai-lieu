package vn.kltn.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
public class PreviewPageSelectionRequest {
    private Long documentId;
    private List<Integer> pageNumbers; // ví dụ: [1, 2, 5]
}
