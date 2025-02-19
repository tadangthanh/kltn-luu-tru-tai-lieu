package vn.kltn.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import vn.kltn.common.RepoPermission;

import java.io.Serializable;
import java.util.Set;

@Getter
@Setter
public class RepoMemberRequest implements Serializable {
    private Set<RepoPermission> permissions;
    @NotNull(message = "Yêu cầu nhập id người dùng")
    private Long userId; // thêm thành viên vào repo thì cần id user
    @NotNull(message = "Yêu cầu nhập id kho lưu trữ")
    private Long repoId; // id của kho lưu trữ
}
