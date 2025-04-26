package vn.kltn.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemRequest {
    @NotBlank(message = "Tên không được để trống")
    private String name;
}
