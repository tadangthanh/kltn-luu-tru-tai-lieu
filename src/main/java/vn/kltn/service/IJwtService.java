package vn.kltn.service;

import org.springframework.security.core.GrantedAuthority;
import vn.kltn.common.TokenType;

import java.util.Collection;
import java.util.List;

public interface IJwtService {
    String generateAccessToken(long userId, String email, List<String> authorities);

    String generateRefreshToken(long userId, String email, List<String> authorities);

    String extractEmail(String token, TokenType type);

    String extractSecret(String token);

    String generateTokenConfirmEmail(String email);
    String generateTokenResetPassword(String email);

    boolean isTokenValid(String token, TokenType type);
}
