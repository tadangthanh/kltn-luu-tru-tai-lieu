package vn.kltn.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "permission", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"recipient_id", "item_id"})})
@Getter
@Setter
@NoArgsConstructor
public class Permission extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "recipient_id")
    private User recipient;

    @Setter
    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Enumerated(EnumType.STRING)
    private vn.kltn.common.Permission permission;

    @Column(name = "is_permission_manager")
    private boolean isPermissionManager = false;

    private boolean isHidden = false;

    private boolean isCustomPermission = false;
    @Version
    private Integer version;  // Trường version giúp Hibernate quản lý locking

    public Permission(User recipient, Item item, vn.kltn.common.Permission permission) {
        this.recipient = recipient;
        this.item = item;
        this.permission = permission;
    }

    public Permission copyForItem(Item item) {
        Permission copy = new Permission();
        copy.setPermission(this.getPermission());
        copy.setRecipient(this.getRecipient());
        copy.setItem(item);
        return copy;
    }

    public Permission withRecipient(User recipient) {
        this.setRecipient(recipient);
        return this;
    }

    public Permission withItem(Item item) {
        this.setItem(item);
        return this;
    }

    public Permission withPermission(vn.kltn.common.Permission permission) {
        this.setPermission(permission);
        return this;
    }
}
