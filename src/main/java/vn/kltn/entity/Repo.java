package vn.kltn.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "repo")
public class Repo extends BaseEntity {
    @Column(name = "name")
    private String name;
    @Column(name = "container_name", nullable = false, unique = true)
    private String containerName;
    @Column(name = "description")
    private String description;
    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;
    @Column(name = "max_size_in_gb", nullable = false)
    private Integer maxSizeInGB; // dung luong toi da cua repository
}
