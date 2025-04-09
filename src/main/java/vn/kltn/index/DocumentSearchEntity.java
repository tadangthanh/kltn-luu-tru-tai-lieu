package vn.kltn.index;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@Document(indexName = "documents")
public class DocumentSearchEntity {

    @Id
    private String id;

    private String title;
    private String content;

    private List<String> tags; // để tìm kiếm theo tag dạng keyword

    private Date uploadDate;
}
