package vn.kltn.service;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import vn.kltn.dto.request.AuthChangePassword;
import vn.kltn.dto.request.AuthResetPassword;
import vn.kltn.dto.request.UserRegister;
import vn.kltn.dto.response.TokenResponse;
import vn.kltn.dto.response.UserResponse;
import vn.kltn.entity.User;

import java.util.List;


public interface IUserService extends UserDetailsService {
    void register(UserRegister userRegister);

    TokenResponse getTokenResponse(User user);

    // xac nhan email de kich hoat tai khoan
    void confirmEmail(Long userId, String token);

    void reConfirmEmail(String email);

    void forgotPassword(String email);

    void resetPassword(AuthResetPassword authResetPassword);

    void updatePassword(AuthChangePassword authChangePassword);

    User createFromGoogle(String email, String fullName, String avatarUrl);

    User getUserByEmail(String email);

    User getUserById(Long id);

    User getByEmail(String email);

    UserResponse getInfo();

}
