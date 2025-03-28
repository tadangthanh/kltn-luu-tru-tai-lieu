package vn.kltn.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "folder")
public class Folder extends BaseEntity implements Resource {
    private String name;
    private String description;
    @Column(name = "size", nullable = false)
    private Long size = 0L;
    private LocalDateTime deletedAt;
    @Column(name = "permanent_delete_at")
    private LocalDateTime permanentDeleteAt; // thoi gian xoa vinh vien
    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Folder parent; // thư mục cha

    @OneToMany(mappedBy = "parent", cascade = CascadeType.REMOVE)
    private Set<Folder> children; // thư mục con

    @OneToMany(mappedBy = "parent", cascade = CascadeType.REMOVE)
    private Set<Document> documents; // danh sách tài liệu

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner; // chủ sở hữu

    @Override
    public void setParent(Resource parent) {
        if (parent instanceof Folder) {
            this.parent = (Folder) parent;
        } else if (parent == null) {
            this.parent = null;
        }
    }
}
