package vn.kltn.dto.response;

import lombok.Getter;
import lombok.Setter;
import vn.kltn.common.FileActionType;
import vn.kltn.dto.BaseDto;

@Getter
@Setter
public class FileActivityResponse extends BaseDto {
    private String fileName;
    private String fileId;
    private String authorName;
    private String authorEmail;
    private Long authorId;
    private FileActionType action;
    private String details;
}
