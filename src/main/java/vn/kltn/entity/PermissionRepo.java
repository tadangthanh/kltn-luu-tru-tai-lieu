package vn.kltn.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import vn.kltn.common.RepoPermission;

@Getter
@Setter
@Entity
@Table(name = "permission_repo")
public class PermissionRepo extends BaseEntity {
    @Enumerated(EnumType.STRING)
    @Column(unique = true, nullable = false)
    private RepoPermission permission;
}
