package vn.kltn.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "role")
public class Role extends BaseEntity implements GrantedAuthority {
    private String name;
    @OneToMany(mappedBy = "role")
    private Set<RoleHasPermission> permissions =new HashSet<>();

    @OneToMany(mappedBy = "role")
    private Set<UserHasRole> users =new HashSet<>();
    @Override
    public String getAuthority() {
        return name;
    }
}
