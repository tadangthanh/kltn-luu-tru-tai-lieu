package vn.kltn.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import vn.kltn.common.Permission;

@Getter
@Setter
@Entity
@Table(name = "document_share")
public class DocumentShare extends BaseEntity {
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

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
