package vn.kltn.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import vn.kltn.common.RepoAction;

@Getter
@Setter
@Entity
@Table(name = "repo_activity")
public class RepoActivity extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "repo_id", nullable = false)
    private Repo repo;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(columnDefinition = "TEXT")
    private String details; // Lưu thông tin chi tiết (ví dụ: tên file, nội dung sửa đổi)
    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    private RepoAction action;
}
