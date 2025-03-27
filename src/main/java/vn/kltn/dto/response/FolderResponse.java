package vn.kltn.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import vn.kltn.dto.BaseDto;

import java.time.LocalDateTime;

@Getter
@Setter
public class FolderResponse extends ResourceResponse {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long parentId;
}
