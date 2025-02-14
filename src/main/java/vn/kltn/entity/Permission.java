package vn.kltn.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "permission")
@Getter
@Setter
public class Permission extends BaseEntity{
    private String name;
    private String description;
    @OneToMany(mappedBy = "permission")
    private Set<RoleHasPermission> roles =new HashSet<>();
}
