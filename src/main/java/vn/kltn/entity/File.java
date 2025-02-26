package vn.kltn.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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

    @ManyToMany
    @JoinTable(
            name = "file_tags",
            joinColumns = @JoinColumn(name = "file_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();
}
