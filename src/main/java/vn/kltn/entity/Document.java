package vn.kltn.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "document")
public class Document extends FileSystemEntity {
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
}
