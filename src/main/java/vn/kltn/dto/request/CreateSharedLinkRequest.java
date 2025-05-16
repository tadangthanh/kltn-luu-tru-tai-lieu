package vn.kltn.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CreateSharedLinkRequest {
    @NotNull(message = "itemId is required")
    private Long itemId;
    private LocalDateTime expiresAt; // null nếu không giới hạn thời gian
    private Integer maxViews;     // null nếu không giới hạn lượt
}
