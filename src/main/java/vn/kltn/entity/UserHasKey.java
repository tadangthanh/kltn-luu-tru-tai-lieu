package vn.kltn.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "user_has_key")
public class UserHasKey extends BaseEntity {
    @Column(unique = true, nullable = false, columnDefinition = "TEXT")
    private String publicKey;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
