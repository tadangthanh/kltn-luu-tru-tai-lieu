package vn.kltn.dto.response;

import lombok.Getter;
import lombok.Setter;
import vn.kltn.dto.BaseDto;

@Getter
@Setter
public class FileShareView extends BaseDto {
    private byte[] fileBytes;
    private String fileName;
    private String contentType;

}
