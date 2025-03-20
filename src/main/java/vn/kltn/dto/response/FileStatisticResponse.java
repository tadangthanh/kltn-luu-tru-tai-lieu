package vn.kltn.dto.response;

import lombok.Getter;
import lombok.Setter;
import vn.kltn.dto.BaseDto;

@Getter
@Setter
public class FileStatisticResponse extends BaseDto {
    private Long fileId;
    private Long viewCount;
    private Long downloadCount;
    private Long shareCount;
}
