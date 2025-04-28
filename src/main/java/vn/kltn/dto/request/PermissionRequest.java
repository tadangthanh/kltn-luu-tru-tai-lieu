package vn.kltn.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import vn.kltn.common.Permission;
import vn.kltn.validation.Create;
import vn.kltn.validation.Update;

import java.io.Serializable;

@Getter
@Setter
public class PermissionRequest implements Serializable {
    private Long id;
    @NotNull(message = "Id nguời được chia sẻ không được để trống", groups = {Create.class})
    private Long recipientId;
    @NotNull(message = "Quyền truy cập không được để trống", groups = {Create.class, Update.class})
    private Permission permission;
    private String message;
    private Boolean isDelete;
}
