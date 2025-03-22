package vn.kltn.dto.response;

import lombok.Getter;
import lombok.Setter;
import vn.kltn.dto.BaseDto;

@Getter
@Setter
public class FolderResponse extends BaseDto {
    private String name;
    private String description;
    private String ownerEmail;
    private String ownerName;
}
