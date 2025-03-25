package vn.kltn.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import vn.kltn.common.Permission;

@Getter
@Setter
@Entity
@Table(name = "folder_access")
public class FolderAccess extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "folder_id")
    private Folder folder;
    @ManyToOne
    @JoinColumn(name = "recipient_id")
    private User recipient;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "permission")
    private Permission permission;
}
