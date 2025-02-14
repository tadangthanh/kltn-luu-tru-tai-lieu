package vn.kltn.service;

import org.springframework.security.core.GrantedAuthority;
import vn.kltn.common.TokenType;
import vn.kltn.entity.User;

import java.util.Collection;

public interface IJwtService {
    String generateAccessToken(long userId,String email, Collection<? extends GrantedAuthority> authorities);
    String generateRefreshToken(long userId,String email ,Collection<? extends GrantedAuthority> authorities);
    String extractEmail(String token, TokenType type);


}
