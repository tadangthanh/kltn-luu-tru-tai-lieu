package vn.kltn.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "shared_links")
@Getter
@Setter
public class SharedLink extends BaseEntity {
    // Tài liệu được chia sẻ
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    // Người chia sẻ tài liệu
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_by_user_id", nullable = false)
    private User sharedBy;

    // Token truy cập, gắn vào link chia sẻ
    @Column(name = "access_token", nullable = false, unique = true)
    private String accessToken;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "max_views")
    private Integer maxViews;

    @Column(name = "current_views", nullable = false)
    private Integer currentViews = 0;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
