package vn.kltn.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "file_statistic")
public class FileStatistic extends BaseEntity {
    @OneToOne
    @JoinColumn(name = "file_id", nullable = false, unique = true)
    private File file;

    @Column(name = "view_count", nullable = false)
    private Long viewCount = 0L;

    @Column(name = "download_count", nullable = false)
    private Long downloadCount = 0L;

    @Column(name = "share_count", nullable = false)
    private Long shareCount = 0L;
}
