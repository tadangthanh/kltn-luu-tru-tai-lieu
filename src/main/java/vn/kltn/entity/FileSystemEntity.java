package vn.kltn.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class FileSystemEntity extends BaseEntity implements Resource {
    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Folder parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<FileSystemEntity> children = new HashSet<>();

    @OneToMany(mappedBy = "resource", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Permission> permissions = new HashSet<>();

    @Override
    public void setParent(Resource parent) {
        if (parent instanceof Folder) {
            this.parent = (Folder) parent;
        } else {
            throw new IllegalArgumentException("Parent must be a Folder");
        }
    }
}
