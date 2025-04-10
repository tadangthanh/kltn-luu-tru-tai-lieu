package vn.kltn.index;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@Document(indexName = "documents")
public class DocumentSearchEntity extends BaseSearchEntity {
    private String name;
    private String type;
    private String description;
    private String content;
    @Field(type = FieldType.Keyword)
    private List<String> tags; // để tìm kiếm theo tag dạng keyword
    private Date uploadDate;
}
