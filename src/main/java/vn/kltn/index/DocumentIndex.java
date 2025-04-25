package vn.kltn.index;

import co.elastic.clients.json.JsonData;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
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
@Document(indexName = "documents_index")
@ToString
public class DocumentIndex extends BaseSearchEntity {
    private Long documentId; // ID của tài liệu gốc
    private String description; // Mô tả tài liệu
    private String name; // tên tài liệu
    private String type; // loại tài liệu
    @Field(type = FieldType.Text, analyzer = "folding_analyzer", searchAnalyzer = "folding_analyzer")
    private String content; // Full nội dung text của tài liệu
    @Field(type = FieldType.Keyword)
    private List<String> tags;
    private Long ownerId;
    @Field(type = FieldType.Keyword)
    private List<Long> sharedWith; // danh sách id user được truy cập tài liệu
    private boolean isDeleted = false;

    // Custom map giữ lại content cũ nếu content mới bị null
    public Map<String, Object> toPartialUpdateMap() {
        Map<String, Object> map = new HashMap<>();
        if (this.name != null) map.put("name", this.name);
        if (this.description != null) map.put("description", this.description);
        if (this.type != null) map.put("type", this.type);
        if (this.content != null) map.put("content", this.content);
        if (this.tags != null) map.put("tags", this.tags);
        if (this.ownerId != null) map.put("ownerId", this.ownerId);
        if (this.sharedWith != null) map.put("sharedWith", this.sharedWith);
        map.put("isDeleted", this.isDeleted);
        if (this.getUpdatedAt() != null) map.put("updatedAt", this.getUpdatedAt());
        if (this.getUpdatedBy() != null) map.put("updatedBy", this.getUpdatedBy());
        return map;
    }


}
