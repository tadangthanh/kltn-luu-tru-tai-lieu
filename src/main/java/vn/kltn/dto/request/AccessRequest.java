package vn.kltn.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import vn.kltn.common.Permission;

import java.io.Serializable;

@Getter
@Setter
public class AccessRequest implements Serializable {
    @NotBlank(message = "Email nguời được chia sẻ không được để trống")
    private String recipientEmail;
    @NotNull(message = "Quyền truy cập không được để trống")
    private Permission permission;
    private String message;
}
