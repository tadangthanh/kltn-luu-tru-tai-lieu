package vn.kltn.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "conversation_file")
@Getter
@Setter
public class ConversationFile extends BaseEntity {
    @Column(name = "name", nullable = false,unique = true)
    private String name;
    @Column(name = "original_file_name", nullable = false)
    private String originalFileName;
    private LocalDateTime expirationTime;
    private LocalDateTime createTime;
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;
    @OneToMany(mappedBy = "conversationFile", cascade = CascadeType.ALL)
    private List<Conversation> conversations;
}
