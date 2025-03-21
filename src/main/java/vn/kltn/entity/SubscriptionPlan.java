package vn.kltn.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "subscription_plan")
public class SubscriptionPlan extends BaseEntity {
    @Column(nullable = false, unique = true)
    private String name;
    @Column(nullable = false)
    private int maxReposPerMember; // Số repo tối đa

    @Column(nullable = false)
    private int maxMembersPerRepo; // Số thành viên tối đa trong repo
    @Column(name = "max_storage", nullable = false)
    private Long maxStorage; // Đơn vị: Byte (VD: 10GB = 10 * 1024 * 1024 * 1024)

    @Column(nullable = false)
    private BigDecimal price;
    @Column(columnDefinition = "TEXT", name = "description")
    private String description;
}
