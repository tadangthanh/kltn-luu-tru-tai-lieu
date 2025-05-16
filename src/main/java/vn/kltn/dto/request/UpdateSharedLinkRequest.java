package vn.kltn.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UpdateSharedLinkRequest {
    private LocalDateTime expiresAt;
    private Integer maxViews;
}
