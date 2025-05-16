package vn.kltn.dto.response;

import lombok.Getter;
import lombok.Setter;
import vn.kltn.dto.BaseDto;

import java.time.LocalDateTime;

@Getter
@Setter
public class SharedLinkResponse extends BaseDto {
    private Long itemId;
    private String accessToken;
    private LocalDateTime expiresAt;
    private Integer maxViews;
    private Integer currentViews;
    private Boolean isActive;
}
