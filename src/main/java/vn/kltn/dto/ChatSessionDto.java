package vn.kltn.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatSessionDto extends BaseDto {
    private String name;
    private Long userId;
}
