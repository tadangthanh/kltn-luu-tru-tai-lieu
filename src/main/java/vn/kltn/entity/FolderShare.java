package vn.kltn.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import vn.kltn.common.Permission;

@Getter
@Setter
@Entity
@Table(name = "folder_share")
public class FolderShare extends BaseEntity {
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "folder_id", nullable = false)
    private Folder folder;

    @Column(name = "token", nullable = false)
    private String token;

    @Column(name = "expired_At", nullable = false)
    private Long expiredAt;

    @Column(name = "password", columnDefinition = "TEXT")
    private String password;

    @Column(name = "permission", nullable = false)
    private Permission permission;

    private String linkShare;

}
