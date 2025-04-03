package vn.kltn.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "permission",     uniqueConstraints = {
        @UniqueConstraint(columnNames = {"recipient_id", "resource_id"})})
@Getter
@Setter
public class Permission extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "recipient_id")
    private User recipient;

    @ManyToOne
    @JoinColumn(name = "resource_id", nullable = false)
    private FileSystemEntity resource;
    @Enumerated(EnumType.STRING)
    private vn.kltn.common.Permission permission;

    private boolean isCustomPermission = false;
}
