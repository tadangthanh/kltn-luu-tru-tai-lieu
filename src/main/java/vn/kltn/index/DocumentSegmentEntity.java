package vn.kltn.index;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;


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
    @Field(type = FieldType.Text, analyzer = "folding_analyzer", searchAnalyzer = "folding_analyzer")
    private String content;
    @Field(type = FieldType.Keyword)
    private List<String> tags;
    private int segmentNumber;
    private Long ownerId;
    @Field(type = FieldType.Keyword)
    private List<Long> sharedWith;
    private boolean isDeleted = false;
}
