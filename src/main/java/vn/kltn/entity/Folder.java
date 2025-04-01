package vn.kltn.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "folder")
public class Folder extends FileSystemEntity {
    private String name;
    private String description;
    @Column(name = "size", nullable = false)
    private Long size = 0L;
    private LocalDateTime deletedAt;
    @Column(name = "permanent_delete_at")
    private LocalDateTime permanentDeleteAt; // thoi gian xoa vinh vien
    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner; // chủ sở hữu
}
