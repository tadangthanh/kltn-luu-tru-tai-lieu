package vn.kltn.service.impl;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import vn.kltn.common.TokenType;
import vn.kltn.exception.InvalidDataException;
import vn.kltn.exception.UnauthorizedException;
import vn.kltn.service.IJwtService;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static vn.kltn.common.TokenType.*;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "JWT-SERVICE")
public class JwtServiceImpl implements IJwtService {
    @Value("${jwt.expirationMinutes}")
    private long expiryMinutes;
    @Value("${jwt.accessKey}")
    private String accessKey;
    @Value("${jwt.refreshKey}")
    private String refreshKey;
    @Value("${jwt.confirmationKey}")
    private String confirmationKey;
    @Value("${jwt.expiryDay}")
    private long expiryDay;
    @Value("${jwt.expirationMinutesConfirm}")
    private long expiryMinutesConfirm;

    @Override
    public String generateAccessToken(long userId, String email, List<String> authorities) {
        log.info("Generate access token for user with id: {}", userId);
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", authorities);
        return generateAccessToken(claims, email);
    }

    @Override
    public String generateRefreshToken(long userId, String email, List<String> authorities) {
        log.info("Generate refresh token for user with id: {}", userId);
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", authorities);
        return generateRefreshToken(claims, email);
    }

    @Override
    public String extractEmail(String token, TokenType type) {
        log.info("Extract email from token: {}", token);
        return extractClaims(type, token, Claims::getSubject);
    }

    @Override
    public String extractSecret(String token) {
        log.info("Extract secret from token: {}", token);
        return extractClaims(TokenType.CONFIRMATION_TOKEN, token, claims -> claims.get("secret", String.class));
    }

    @Override
    public String generateTokenConfirmEmail(String email) {
        log.info("Generate token confirm email for email: {}", email);
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email);
        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuedAt(new Date()) // thoi diem tao
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * getExpiryMinutes(CONFIRMATION_TOKEN)))
                .signWith(getKey(CONFIRMATION_TOKEN))
                .compact();
    }

    @Override
    public boolean isTokenValid(String token, TokenType type) {
        try {
            extractAllClaim(token, type);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ham Function nhan vao kieu T (Claims) va tra ve kieu R(gia tri tra ve cua ham claimsExtractor.apply(claims))
    private <T> T extractClaims(TokenType type, String token, Function<Claims, T> claimsExtractor) {
        final Claims claims = extractAllClaim(token, type);
        return claimsExtractor.apply(claims);
    }

    private Claims extractAllClaim(String token, TokenType type) {
        try {
            return Jwts.parser()
                    .verifyWith(getKey(type))
                    .build().parseSignedClaims(token).getPayload();
        } catch (ExpiredJwtException | SignatureException e) {
            throw new AccessDeniedException("Access denied!, error: " + e.getMessage());
        }
    }

    private String generateAccessToken(Map<String, Object> claims, String email) {
        log.info("Generate access token for email: {}", email);
        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuedAt(new Date()) // thoi diem tao
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * getExpiryMinutes(ACCESS_TOKEN)))
                .signWith(getKey(ACCESS_TOKEN))
                .compact();
    }

    private String generateRefreshToken(Map<String, Object> claims, String email) {
        log.info("Generate refresh token for email: {}", email);
        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuedAt(new Date()) // thoi diem tao
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * getExpiryMinutes(REFRESH_TOKEN)))
                .signWith(getKey(REFRESH_TOKEN))
                .compact();
    }

    private SecretKey getKey(TokenType type) {
        String key = switch (type) {
            case ACCESS_TOKEN -> accessKey;
            case REFRESH_TOKEN -> refreshKey;
            case CONFIRMATION_TOKEN -> confirmationKey;
            default -> throw new InvalidDataException("Invalid token type");
        };
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(key));
    }

    private long getExpiryMinutes(TokenType type) {
        return switch (type) {
            case ACCESS_TOKEN -> expiryMinutes;
            case REFRESH_TOKEN -> expiryDay * 24 * 60;
            case CONFIRMATION_TOKEN -> expiryMinutesConfirm;
            default -> throw new InvalidDataException("Invalid token type");
        };
    }

}
