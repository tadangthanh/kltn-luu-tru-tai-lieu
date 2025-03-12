package vn.kltn.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import vn.kltn.common.RepoPermission;
import vn.kltn.validation.Create;

import java.io.Serializable;
import java.util.Set;

@Getter
@Setter
public class RepoMemberRequest implements Serializable {
    private Set<RepoPermission> permissions;
    @NotNull(message = "Yêu cầu nhập id người dùng", groups = Create.class)
    private Long userId; // thêm thành viên vào repo thì cần id user
}
