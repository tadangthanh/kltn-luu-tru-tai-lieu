package vn.kltn.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentRequest {
    private String description;
    private TagRequest[] tags;
}
