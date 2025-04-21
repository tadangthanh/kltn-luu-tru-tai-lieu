package vn.kltn.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

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

    @ManyToOne
    @JoinColumn(name = "chat_session_id", nullable = false)
    private ChatSession chatSession;


}
