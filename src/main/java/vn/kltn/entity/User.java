package vn.kltn.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import vn.kltn.common.UserStatus;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "user")
public class User extends BaseEntity implements UserDetails {
    @Column(unique = true, nullable = false)
    private String email;
    @Column(columnDefinition = "TEXT")
    private String password;
    @Column(nullable = false)
    private String fullName;
    private String avatarUrl;
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private UserStatus status;
    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    private Set<UserHasRole> roles = new HashSet<>();
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<UserHasKey> keys = new HashSet<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // lay ra roles
        List<Role> roleList = roles.stream().map(UserHasRole::getRole).toList();
        // lay ra ten roles
        List<String> roleNames = roleList.stream().map(Role::getName).toList();
        // chuyen doi ten roles thanh GrantedAuthority
        return roleNames.stream().map(SimpleGrantedAuthority::new).toList();
    }

    @Override
    public String getUsername() {
        return getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserStatus.ACTIVE.equals(status);
    }
}
