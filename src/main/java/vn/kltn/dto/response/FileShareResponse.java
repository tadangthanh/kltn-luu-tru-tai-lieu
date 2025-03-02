package vn.kltn.dto.response;

import lombok.Getter;
import lombok.Setter;
import vn.kltn.dto.BaseDto;

@Getter
@Setter
public class FileShareResponse extends BaseDto {
    private String token;
    private String expireAt;
    private String fileName;
}
