package vn.kltn.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class FileRequest implements Serializable {
    private String description;
    @NotBlank(message = "Tên file không được để trống")
    private String fileName;
    private TagRequest[] tags;
    @NotNull(message = "Trạng thái public không được để trống")
    private boolean isPublic;
}
