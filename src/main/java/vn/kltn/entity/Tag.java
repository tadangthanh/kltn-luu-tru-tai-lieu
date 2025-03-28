package vn.kltn.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tag")
@Getter
@Setter
public class Tag extends BaseEntity {
    @Column(unique = true, nullable = false)
    private String name;
    private String description;
}
