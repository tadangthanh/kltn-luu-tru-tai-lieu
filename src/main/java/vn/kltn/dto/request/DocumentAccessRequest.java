package vn.kltn.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import vn.kltn.common.Permission;

import java.io.Serializable;

@Getter
@Setter
public class DocumentAccessRequest implements Serializable {
    @NotBlank(message = "Email nguời được chia sẻ không được để trống")
    private String recipientEmail;
    private Permission permission;
}
