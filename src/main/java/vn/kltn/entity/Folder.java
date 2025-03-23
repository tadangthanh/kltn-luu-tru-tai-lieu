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
public class Folder extends BaseEntity {
    private String name;
    private String description;
    private LocalDateTime deletedAt;
    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Folder parent; // thư mục cha

    @OneToMany(mappedBy = "parent", cascade = CascadeType.REMOVE)
    private Set<Folder> children; // thư mục con

    @OneToMany(mappedBy = "folder", cascade = CascadeType.REMOVE)
    private Set<Document> documents; // danh sách tài liệu

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user; // chủ sở hữu
}
