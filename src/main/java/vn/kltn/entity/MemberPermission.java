package vn.kltn.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "member_permission")
public class MemberPermission extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "member_id")
    private RepoMember member;
    @ManyToOne
    @JoinColumn(name = "permission_id")
    private PermissionRepo permission;
}
