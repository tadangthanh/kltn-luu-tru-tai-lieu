package vn.kltn.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import vn.kltn.common.RoleName;

@Getter
@Setter
@Entity
@Table(name = "member_role")
public class MemberRole extends BaseEntity {
    @Enumerated(EnumType.STRING)
    @Column(unique = true)
    private RoleName name;
    private String description;
}
