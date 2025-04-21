package vn.kltn.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConversationRequest {
    @NotBlank(message = "Question is required")
    private String question;
    private String answer;
    @NotNull(message = "chatSession id is required")
    private Long chatSessionId;
}
