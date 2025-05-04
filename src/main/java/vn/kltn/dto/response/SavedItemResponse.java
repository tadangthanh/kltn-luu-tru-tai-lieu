package vn.kltn.dto.response;

import lombok.Getter;
import lombok.Setter;
import vn.kltn.dto.BaseDto;

@Getter
@Setter
public class SavedItemResponse extends BaseDto {
    private Long id;
    private ItemResponse item;
}
