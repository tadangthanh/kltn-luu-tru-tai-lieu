package vn.kltn.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class RepositoryRequestDto implements Serializable {
    @NotBlank(message = "Tên repository không được để trống")
    private String name;
    private String description;
}
