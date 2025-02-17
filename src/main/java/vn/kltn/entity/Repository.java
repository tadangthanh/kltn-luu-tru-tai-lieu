package vn.kltn.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "repo")
public class Repository extends BaseEntity{
    @Column(name = "name")
    private String name;
    @Column(name = "uuid",nullable = false)
    private String uuid;
    @Column(name = "description")
    private String description;
    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;
}
