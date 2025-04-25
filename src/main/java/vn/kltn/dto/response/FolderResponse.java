package vn.kltn.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FolderResponse extends ItemResponse {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long parentId;
}
