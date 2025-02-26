package vn.kltn.dto.response;

import lombok.Getter;
import lombok.Setter;
import vn.kltn.dto.BaseDto;

@Getter
@Setter
public class TagResponse extends BaseDto {
    private String name;
    private String description;
}
