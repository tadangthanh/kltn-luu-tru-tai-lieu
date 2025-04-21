package vn.kltn.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AssistantFileRequest {
    @NotBlank(message = "File name is required")
    private String name;
    @NotBlank(message = "uri name is required")
    private String uri;

    @NotBlank(message = "Original file name is required")
    private String originalFileName;

    @NotNull(message = "Expiration time is required")
    private LocalDateTime expirationTime;

    @NotNull(message = "Create time is required")
    private LocalDateTime createTime;

    @NotNull(message = "Chat session ID is required")
    private Long chatSessionId;
}
