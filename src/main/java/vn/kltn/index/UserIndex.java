package vn.kltn.index;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.springframework.data.elasticsearch.annotations.Document;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Document(indexName = "users_index")
public class UserIndex extends BaseSearchEntity {
    private String email;
    private String fullName;
    private String avatarUrl;
    private String status;
}
