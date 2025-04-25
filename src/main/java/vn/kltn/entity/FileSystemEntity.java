package vn.kltn.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class FileSystemEntity extends BaseEntity implements Resource {
    private String name;
    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Folder parent;
    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;
    private LocalDateTime deletedAt;
    @Column(name = "permanent_delete_at")
    private LocalDateTime permanentDeleteAt; // thoi gian xoa vinh vien
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<FileSystemEntity> children = new HashSet<>();

    @OneToMany(mappedBy = "resource", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Permission> permissions = new HashSet<>();

    @Override
    public void setParent(Resource parent) {
        if (parent instanceof Folder) {
            this.parent = (Folder) parent;
        } else {
            this.parent = null; // Hoặc throw Exception nếu cần
        }
    }

}
