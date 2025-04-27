package vn.kltn.index;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Getter
@Setter
public abstract class BaseSearchEntity {
    @Id
    private String id;
    @Field(type = FieldType.Date)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date createdAt;

    @Field(type = FieldType.Keyword)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String createdBy;

    @Field(type = FieldType.Date)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date updatedAt;

    @Field(type = FieldType.Keyword)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String updatedBy;

}
