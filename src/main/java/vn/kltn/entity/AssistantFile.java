package vn.kltn.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "assistant_file")
@Getter
@Setter
public class AssistantFile extends BaseEntity {
    @Column(name = "name", nullable = false, unique = true)
    private String name;
    @Column(name = "original_file_name", nullable = false)
    private String originalFileName;
    private LocalDateTime expirationTime;
    private LocalDateTime createTime;
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;
    @OneToMany(mappedBy = "assistantFile", cascade = CascadeType.ALL)
    private List<Conversation> conversations;
}
