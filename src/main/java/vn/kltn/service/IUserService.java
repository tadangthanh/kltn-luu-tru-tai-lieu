package vn.kltn.service;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import vn.kltn.dto.request.AuthChangePassword;
import vn.kltn.dto.request.AuthResetPassword;
import vn.kltn.dto.request.UserRegister;
import vn.kltn.dto.response.TokenResponse;
import vn.kltn.entity.User;


public interface IUserService extends UserDetailsService {
    void register(UserRegister userRegister);

    // xac nhan email de kich hoat tai khoan
    void confirmEmail(Long userId, String token);

    void reConfirmEmail(String email);

    void forgotPassword(String email);

    void resetPassword(AuthResetPassword authResetPassword);

    void updatePassword(AuthChangePassword authChangePassword);

    User getUserByEmail(String email);

    User getUserById(Long id);

    TokenResponse loginWithGoogle(OAuth2User oAuth2User);

    User mapOAuth2UserToUser(OAuth2User oAuth2User);

    User getBySub(String sub);

    User getByEmail(String email);

    User savePublicKeyByUserId(Long id, String publicKey);
}
