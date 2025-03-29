package vn.kltn.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "document")
public class Document extends BaseEntity implements Resource {
    private String name;
    private String type;
    private String blobName;
    private Long size;
    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;
    private Integer version;
    private String description;
    @Column(name = "permanent_delete_at")
    private LocalDateTime permanentDeleteAt; // thoi gian xoa vinh vien
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Folder parent;
    @OneToMany(mappedBy = "resource")
    private List<DocumentAccess> documentAccessList;

    @Override
    public void setParent(Resource parent) {
        if (parent instanceof Folder) {
            this.parent = (Folder) parent;
        } else if (parent == null) {
            this.parent = null;
        }
    }
}
