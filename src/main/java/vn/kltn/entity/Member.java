package vn.kltn.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import vn.kltn.common.MemberStatus;

@Getter
@Setter
@Entity
@Table(name = "member",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "repo_id"})}
)
public class Member extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    @ManyToOne
    @JoinColumn(name = "repo_id")
    private Repo repo;
    private String sasToken;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MemberStatus status;

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private MemberRole role;
}
