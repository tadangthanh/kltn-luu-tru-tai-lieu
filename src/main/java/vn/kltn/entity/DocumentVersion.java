package vn.kltn.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "document_version")
public class DocumentVersion extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "document_id")
    private Document document;

    private Integer version;

    private String blobName;

    private String type; // docx, pdf...

    private Long size;

    private LocalDateTime expiredAt; // xóa sau 7 ngày
}
