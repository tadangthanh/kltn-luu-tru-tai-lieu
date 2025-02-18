package vn.kltn.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class RepoRequestDto implements Serializable {
    @NotBlank(message = "Tên repository không được để trống")
    private String name;
    private String description;
}
