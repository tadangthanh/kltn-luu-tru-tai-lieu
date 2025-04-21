package vn.kltn.dto.response;

import lombok.Getter;
import lombok.Setter;
import vn.kltn.dto.BaseDto;

import java.time.LocalDateTime;

@Getter
@Setter
public class AssistantFileDto extends BaseDto {
    private String name;

    private String originalFileName;

    private LocalDateTime expirationTime;

    private LocalDateTime createTime;
    private String uri;

}
