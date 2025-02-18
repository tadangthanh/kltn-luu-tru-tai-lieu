package vn.kltn.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import vn.kltn.common.RepoPermission;

import java.io.Serializable;
import java.util.Set;

@Getter
@Setter
public class MemberRepoRequest implements Serializable {
    @NotEmpty(message = "Yêu cầu nhập ít nhất một quyền")
    @NotNull(message = "Yêu cầu nhập ít nhất một quyền")
    private Set<RepoPermission> permissions;
    @NotNull(message = "Yêu cầu nhập id người dùng")
    private Long userId;
}
