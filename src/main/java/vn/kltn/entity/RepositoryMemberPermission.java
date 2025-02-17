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
@Table(name = "repository_member_permission")
public class RepositoryMemberPermission extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "member_id")
    private RepositoryMember member;
    @ManyToOne
    @JoinColumn(name = "permission_id")
    private PermissionRepository permission;
}
