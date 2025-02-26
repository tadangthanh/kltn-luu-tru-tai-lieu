package vn.kltn.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class TagRequest implements Serializable {
    @NotBlank(message = "Tag name is required")
    private String name;
    private String description;
}
