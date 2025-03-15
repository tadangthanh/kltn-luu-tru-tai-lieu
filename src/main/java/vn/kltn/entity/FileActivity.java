package vn.kltn.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import vn.kltn.common.FileActionType;

@Entity
@Getter
@Setter
@Table(name = "file_activity")
public class FileActivity extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "file_id", nullable = false)
    private File file;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(columnDefinition = "TEXT")
    private String details; // Lưu thông tin chi tiết (ví dụ: tên file, nội dung sửa đổi)
    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    private FileActionType action;
}
