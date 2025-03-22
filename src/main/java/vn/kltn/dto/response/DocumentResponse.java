package vn.kltn.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import vn.kltn.dto.BaseDto;

import java.time.LocalDateTime;

@Getter
@Setter
public class DocumentResponse extends BaseDto {
    private String name;
    private String type;
    private Long size;
    private Long folderId;
    private int version;
    private String description;
    private String ownerName;
    private String ownerEmail;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDateTime deletedAt;
}
