package vn.kltn.index;

import co.elastic.clients.json.JsonData;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
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

    public Map<String, JsonData> toParamMap() {
        Map<String, JsonData> map = new HashMap<>();
        if (this.name != null) map.put("name", JsonData.of(this.name));
        if (this.description != null) map.put("description", JsonData.of(this.description));
        if (this.type != null) map.put("type", JsonData.of(this.type));
        if (this.content != null) map.put("content", JsonData.of(this.content));
        if (this.tags != null) map.put("tags", JsonData.of(this.tags));
        if (this.segmentNumber != 0) map.put("segmentNumber", JsonData.of(this.segmentNumber));
        if (this.ownerId != null) map.put("ownerId", JsonData.of(this.ownerId));
        if (this.sharedWith != null) map.put("sharedWith", JsonData.of(this.sharedWith));
        map.put("isDeleted", JsonData.of(this.isDeleted)); // boolean mặc định là false nên luôn truyền
        if (this.getUpdatedAt() != null) map.put("updatedAt", JsonData.of(this.getUpdatedAt()));
        if (this.getUpdatedBy() != null) map.put("updatedBy", JsonData.of(this.getUpdatedBy()));
        return map;
    }


}
