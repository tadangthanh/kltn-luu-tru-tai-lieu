package vn.kltn.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConversationDto extends BaseDto {
    @NotBlank(message = "Question is required")
    private String question;
    private String answer;
    @NotNull(message = "chatSession id is required")
    private Long chatSessionId;
}
