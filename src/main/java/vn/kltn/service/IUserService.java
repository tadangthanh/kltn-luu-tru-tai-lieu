package vn.kltn.service;

import org.springframework.security.core.userdetails.UserDetailsService;
import vn.kltn.dto.request.AuthChangePassword;
import vn.kltn.dto.request.AuthResetPassword;
import vn.kltn.dto.request.UserRegister;

public interface IUserService extends UserDetailsService {
    void register(UserRegister userRegister);

    // xac nhan email de kich hoat tai khoan
    void confirmEmail(Long userId, String token);

    void reConfirmEmail(String email);

    void forgotPassword(String email);

    void resetPassword(AuthResetPassword authResetPassword);
    void updatePassword(AuthChangePassword authChangePassword);
}
