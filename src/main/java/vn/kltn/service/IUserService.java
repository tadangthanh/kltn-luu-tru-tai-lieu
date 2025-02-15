package vn.kltn.service;

import org.springframework.security.core.userdetails.UserDetailsService;
import vn.kltn.dto.request.AuthRequest;
import vn.kltn.dto.request.UserRegister;

public interface IUserService extends UserDetailsService {
    void register(UserRegister userRegister);

}
