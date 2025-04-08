package vn.kltn.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "preview_image",uniqueConstraints = {
        @UniqueConstraint(columnNames = {"pageNumber", "document_id"})})
@Getter
@Setter
public class PreviewImage extends BaseEntity {
    @Column(name = "page_number", nullable = false) // page bat dau tu 0
    private int pageNumber;
    @Column(name = "image_blob_name", nullable = false)
    private String imageBlobName; // tên file ảnh trên blob
    @ManyToOne
    @JoinColumn(name = "document_id")
    private Document document;
}
