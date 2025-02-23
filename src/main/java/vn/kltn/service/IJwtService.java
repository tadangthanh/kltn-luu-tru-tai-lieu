package vn.kltn.service;

import vn.kltn.common.TokenType;

import java.util.List;
import java.util.Map;

public interface IJwtService {
    String generateAccessToken(long userId, String email, List<String> authorities);

    String generateRefreshToken(long userId, String email, List<String> authorities);

    String extractEmail(String token, TokenType type);

    String extractSecret(String token);

    String generateTokenConfirmEmail(String email);

    String generateTokenResetPassword(String email);

    boolean isTokenValid(String token, TokenType type);
     String generateToken(TokenType tokenType, Map<String, Object> claims, String email);
}
