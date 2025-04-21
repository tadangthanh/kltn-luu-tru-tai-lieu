package vn.kltn.dto.response;

import lombok.Getter;
import lombok.Setter;
import vn.kltn.dto.BaseDto;

@Getter
@Setter
public class ConversationDto extends BaseDto {
    private String question;
    private String answer;
}
