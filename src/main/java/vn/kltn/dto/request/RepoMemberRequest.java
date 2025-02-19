package vn.kltn.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import vn.kltn.common.RepoPermission;
import vn.kltn.validation.Create;
import vn.kltn.validation.Update;

import java.io.Serializable;
import java.util.Set;

@Getter
@Setter
public class RepoMemberRequest implements Serializable {
    private Set<RepoPermission> permissions;
    @NotNull(message = "Yêu cầu nhập id người dùng", groups = {Create.class})
    private Long userId; // thêm thành viên vào repo thì cần id user
    @NotNull(message = "Yêu cầu nhập id thành viên", groups = {Update.class})
    private Long memberId; // cập nhật member thì cần id của member
    @NotNull(message = "Yêu cầu nhập id kho lưu trữ", groups = {Create.class, Update.class})
    private Long repoId; // id của kho lưu trữ
}
