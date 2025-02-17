package vn.kltn.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import vn.kltn.common.RepositoryPermission;

@Getter
@Setter
@Entity
@Table(name = "permission_repository")
public class PermissionRepository extends BaseEntity{
    @Enumerated(EnumType.STRING)
    @Column(unique = true,nullable = false)
    private RepositoryPermission permission;
}
