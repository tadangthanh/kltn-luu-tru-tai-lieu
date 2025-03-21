package vn.kltn.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "user_subscription")
public class UserSubscription extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "plan_id", nullable = false)
    private SubscriptionPlan plan;

    @Column(name = "used_storage", nullable = false)
    private Long usedStorage = 0L; // Dung lượng đã sử dụng

    private LocalDateTime expireAt; // Thời gian hết hạn của gói (NULL nếu Free plan)
}
