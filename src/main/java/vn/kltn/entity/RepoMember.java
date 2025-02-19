package vn.kltn.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import vn.kltn.common.RepoPermission;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "repo_member",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "repo_id"})}
)
public class RepoMember extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    @ManyToOne
    @JoinColumn(name = "repo_id")
    private Repo repo;
    private String sasToken;
    // Lưu danh sách quyền trực tiếp trong bảng repo_member
    // no se tao 1 table repo_member_permission voi 2 cot member_id va permission
    @ElementCollection(targetClass = RepoPermission.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "repo_member_permission", joinColumns = @JoinColumn(name = "member_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "permission")
    private Set<RepoPermission> permissions = new HashSet<>();
}
