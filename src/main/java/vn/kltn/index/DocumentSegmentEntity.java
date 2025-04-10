package vn.kltn.index;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "document_segments")
public class DocumentSegmentEntity extends BaseSearchEntity {
    private Long documentId; // ID của tài liệu gốc
    private String description; // Mô tả tài liệu
    private String name;
    private String type;
    @Field(type = FieldType.Text)
    private String content;
    private int segmentNumber;
}
