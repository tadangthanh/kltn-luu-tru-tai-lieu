package vn.kltn.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "document_has_tag")
public class DocumentHasTag extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;
    @ManyToOne
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;
}
