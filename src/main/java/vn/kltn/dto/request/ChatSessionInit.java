package vn.kltn.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import vn.kltn.dto.response.AssistantFileDto;
import vn.kltn.dto.response.ConversationDto;

import java.util.List;

@Getter
@Setter
public class ChatSessionInit {
    private String name;
    @NotEmpty(message = "At least one file is required")
    private List<AssistantFileDto> assistantFiles;
    private ConversationDto conversation;
}
