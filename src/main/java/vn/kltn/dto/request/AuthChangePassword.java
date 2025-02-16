package vn.kltn.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthChangePassword {
    @NotBlank(message = "Bạn cần nhập mật khẩu cũ")
    private String currentPassword;
    @NotBlank(message = "Bạn cần nhập mật khẩu mới")
    private String newPassword;
    @NotBlank(message = "Bạn cần nhập lại mật khẩu mới")
    private String confirmPassword;
    @NotBlank(message = "Token không được rỗng")
    private String token;
}
