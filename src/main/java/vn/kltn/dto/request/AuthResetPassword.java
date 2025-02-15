package vn.kltn.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthResetPassword {
    @NotBlank(message = "Token không được để trống")
    private String token;
    @NotBlank(message = "Mật khẩu không được để trống")
    private String newPassword;
    @NotBlank(message = "Xác nhận mật khẩu không được để trống")
    private String confirmPassword;
}
