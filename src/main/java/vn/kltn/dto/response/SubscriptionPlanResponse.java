package vn.kltn.dto.response;

import lombok.Getter;
import lombok.Setter;
import vn.kltn.dto.BaseDto;

import java.math.BigDecimal;

@Getter
@Setter
public class SubscriptionPlanResponse extends BaseDto {
    private String name;
    private int maxReposPerMember;
    private int maxMembersPerRepo;
    private Long maxStorage;
    private BigDecimal price;
}
