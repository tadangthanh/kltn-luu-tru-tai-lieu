package vn.kltn.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import vn.kltn.dto.BaseDto;

import java.time.LocalDateTime;

@Getter
@Setter
public  class ItemResponse extends BaseDto {
    private String name;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long parentId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String description;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String ownerName;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String ownerEmail;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDateTime deletedAt;
    private Long size;

}
