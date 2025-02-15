package vn.kltn.service;

import org.springframework.security.core.userdetails.UserDetailsService;
import vn.kltn.dto.request.UserRegister;

public interface IUserService extends UserDetailsService {
    void register(UserRegister userRegister);

    // xac nhan email de kich hoat tai khoan
    void confirmEmail(Long userId, String secret);
}
