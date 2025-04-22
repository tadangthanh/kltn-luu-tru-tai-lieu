package vn.kltn.dto.response;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import vn.kltn.dto.BaseDto;

import java.time.LocalDateTime;

@Getter
@Setter
public class AssistantFileDto extends BaseDto {
    @NotBlank(message = "Tên file không được để trống")
    private String name;

    @NotBlank(message = "originalFileName file không được để trống")
    private String originalFileName;
    @NotBlank(message = "mimeType file không được để trống")
    private String mimeType;

    private LocalDateTime expirationTime;

    private LocalDateTime createTime;
    @NotBlank(message = "uri file không được để trống")
    private String uri;

}
