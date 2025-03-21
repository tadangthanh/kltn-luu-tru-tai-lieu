package vn.kltn.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import vn.kltn.validation.Create;

import java.io.Serializable;

@Getter
@Setter
public class RepoRequestDto implements Serializable {
    @NotBlank(message = "Tên repository không được để trống", groups = {Create.class})
    private String name;
    @Size(max = 255, message = "Mô tả không quá 255 ký tự")
    private String description;
}
