package vn.kltn.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import vn.kltn.common.TransferStatus;

@Getter
@Setter
@Entity
@Table(name = "owner_ship_transfer", uniqueConstraints = @UniqueConstraint(columnNames = {"document_id", "folder_id", "new_owner_id", "status"}))
public class OwnerShipTransfer extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "document_id")
    private Document document;
    @ManyToOne
    @JoinColumn(name = "folder_id")
    private Folder folder;

    @ManyToOne
    @JoinColumn(name = "new_owner_id", nullable = false)
    private User newOwner;

    @ManyToOne
    @JoinColumn(name = "old_owner_id", nullable = false)
    private User oldOwner;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private TransferStatus status;
}
