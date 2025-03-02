package vn.kltn.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "file_share")
public class FileShare extends BaseEntity {
    @OneToOne
    @JoinColumn(name = "file_id", unique = true)
    private File file;

    private String token;
    @Column(name = "expire_at")
    private LocalDateTime expireAt;
    @Column(name = "password_hash")
    private String passwordHash;
}
