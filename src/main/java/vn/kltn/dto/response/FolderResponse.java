package vn.kltn.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import vn.kltn.dto.BaseDto;

@Getter
@Setter
public class FolderResponse extends BaseDto {
    private String name;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String description;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String ownerEmail;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String ownerName;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long folderParentId;
}
