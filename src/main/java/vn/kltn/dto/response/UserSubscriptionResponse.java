package vn.kltn.dto.response;

import lombok.Getter;
import lombok.Setter;
import vn.kltn.dto.BaseDto;

@Getter
@Setter
public class UserSubscriptionResponse extends BaseDto {
    private SubscriptionPlanResponse subscription;
    private String expireAt;
    private Long userId;
    private Long usedStorage = 0L; // Dung lượng đã sử dụng
}
