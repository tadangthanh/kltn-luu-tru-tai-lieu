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
@Table(name = "file")
public class File extends BaseEntity {
    private String fileName;
    private String fileType;
    private String fileBlobName;
    private String fileUrl;
    private Long fileSize;
    //    @Column(nullable = false, columnDefinition = "TEXT")
    @Column(columnDefinition = "TEXT")
    private String publicKey;
    @ManyToOne
    @JoinColumn(name = "repo_id")
    private Repo repo;
    private boolean isPublic;
    private Integer version;
    private String description;
    private String checkSum;
    //    @Column(nullable = false, columnDefinition = "TEXT")
    @Column(columnDefinition = "TEXT")
    private String signature;
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    @Column(name = "permanent_delete_at")
    private LocalDateTime permanentDeleteAt; // thoi gian xoa vinh vien
    @ManyToOne
    @JoinColumn(name = "deleted_by")
    private Member deletedBy;
    @ManyToOne
    @JoinColumn(name = "uploaded_by")
    private Member uploadedBy;
    @OneToMany(mappedBy = "file", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private Set<FileHasTag> tags = new HashSet<>();
    @OneToOne(mappedBy = "file", cascade = CascadeType.REMOVE)
    private FileShare fileShare;
}
