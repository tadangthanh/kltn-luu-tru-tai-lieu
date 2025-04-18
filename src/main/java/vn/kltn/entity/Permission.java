package vn.kltn.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "permission", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"recipient_id", "resource_id"})})
@Getter
@Setter
@NoArgsConstructor
public class Permission extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "recipient_id")
    private User recipient;

    @Setter
    @ManyToOne
    @JoinColumn(name = "resource_id", nullable = false)
    private FileSystemEntity resource;
    @Enumerated(EnumType.STRING)
    private vn.kltn.common.Permission permission;

    private boolean isCustomPermission = false;

    public Permission(User recipient, Resource resource, vn.kltn.common.Permission permission) {
        this.recipient = recipient;
        this.resource = (FileSystemEntity) resource;
        this.permission = permission;
    }
    public Permission copyForResource(FileSystemEntity newResource) {
        Permission copy = new Permission();
        copy.setPermission(this.getPermission());
        copy.setRecipient(this.getRecipient());
        copy.setResource(newResource);
        return copy;
    }
    public Permission withRecipient(User recipient) {
        this.setRecipient(recipient);
        return this;
    }

    public Permission withResource(FileSystemEntity resource) {
        this.setResource(resource);
        return this;
    }

    public Permission withPermission(vn.kltn.common.Permission permission) {
        this.setPermission(permission);
        return this;
    }
}
