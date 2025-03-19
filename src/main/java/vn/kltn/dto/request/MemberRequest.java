package vn.kltn.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import vn.kltn.validation.Create;

import java.io.Serializable;

@Getter
@Setter
public class MemberRequest implements Serializable {
    private Long roleId; // thay đổi quyền thành viên trong repo
    @NotNull(message = "Yêu cầu nhập id người dùng", groups = Create.class)
    private Long userId; // thêm thành viên vào repo thì cần id user
}
