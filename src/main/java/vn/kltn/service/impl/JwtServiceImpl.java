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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import vn.kltn.common.TokenType;
import vn.kltn.exception.InvalidDataException;
import vn.kltn.service.IJwtService;

import javax.crypto.SecretKey;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static vn.kltn.common.TokenType.ACCESS_TOKEN;
import static vn.kltn.common.TokenType.REFRESH_TOKEN;

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
    @Value("${jwt.expiryDay}")
    private long expiryDay;

    @Override
    public String generateAccessToken(long userId, String email, Collection<? extends GrantedAuthority> authorities) {
        log.info("Generate access token for user with id: {}", userId);
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", authorities);
        return generateAccessToken(claims, email);
    }

    @Override
    public String generateRefreshToken(long userId, String email, Collection<? extends GrantedAuthority> authorities) {
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
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * expiryMinutes))
                .signWith(getKey(ACCESS_TOKEN))
                .compact();
    }

    private String generateRefreshToken(Map<String, Object> claims, String email) {
        log.info("Generate refresh token for email: {}", email);
        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuedAt(new Date()) // thoi diem tao
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * expiryDay))
                .signWith(getKey(REFRESH_TOKEN))
                .compact();
    }

    private SecretKey getKey(TokenType type) {
        String key = switch (type) {
            case ACCESS_TOKEN -> accessKey;
            case REFRESH_TOKEN -> refreshKey;
            default -> throw new InvalidDataException("Invalid token type");
        };
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(key));
    }

}
