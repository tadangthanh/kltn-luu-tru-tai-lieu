package vn.kltn.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import vn.kltn.common.Permission;

@Getter
@Setter
@Entity
@Table(name = "document_access")
public class DocumentAccess extends BaseEntity implements AccessResource {
    @ManyToOne
    @JoinColumn(name = "resource_id")
    private Document resource;
    @ManyToOne
    @JoinColumn(name = "recipient_id")
    private User recipient; //nguời được cấp quyền truy cập
    @Enumerated(EnumType.STRING)
    @Column(name = "permission", nullable = false)
    private Permission permission;

    @Override
    public <T extends Resource> void setResource(T resource) {
        this.resource = (Document) resource;
    }
}
