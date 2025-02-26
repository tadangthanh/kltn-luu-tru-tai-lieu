package vn.kltn.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

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
    @ManyToOne
    @JoinColumn(name = "repo_id")
    private Repo repo;

    private boolean isPublic;
    private Integer version;
    private String description;
    private String checkSum;
    @ManyToOne
    @JoinColumn(name = "uploaded_by")
    private RepoMember uploadedBy;
}
