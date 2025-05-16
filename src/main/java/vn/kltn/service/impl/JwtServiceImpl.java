package vn.kltn.service.impl;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
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
    @Value("${jwt.invitationKey}")
    private String invitationKey;
    @Value("${jwt.resetPasswordKey}")
    private String resetPasswordKey;
    @Value("${jwt.refreshKey}")
    private String refreshKey;
    @Value("${jwt.confirmationKey}")
    private String confirmationKey;
    @Value("${jwt.expiryDay}")
    private long expiryDay;
    @Value("${jwt.expirationMinutesConfirm}")
    private long expiryMinutesConfirm;
    @Value("${jwt.expirationMinutesResetPassword}")
    private long expiryMinutesResetPassword;
    @Value("${jwt.expirationDayInvitation}")
    private long expiryDayInvitation;


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
    public String generateTokenConfirmEmail(String email) {
        log.info("Generate token confirm email for email: {}", email);
        return generateToken(CONFIRMATION_TOKEN, null, email);
    }

    @Override
    public String generateToken(TokenType tokenType, Map<String, Object> claims, String email) {
        log.info("Generate token {} for email: {}", tokenType, email);
        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuedAt(new Date()) // thoi diem tao
                .expiration(getExpirationDate(tokenType))
                .signWith(getKey(tokenType))
                .compact();
    }

    private Date getExpirationDate(TokenType tokenType) {
        return switch (tokenType) {
            case ACCESS_TOKEN -> new Date(System.currentTimeMillis() + 1000 * 60 * expiryMinutes);
            case REFRESH_TOKEN -> new Date(System.currentTimeMillis() + 1000 * 60 * expiryDay * 24 * 60);
            case CONFIRMATION_TOKEN -> new Date(System.currentTimeMillis() + 1000 * 60 * expiryMinutesConfirm);
            case RESET_PASSWORD_TOKEN -> new Date(System.currentTimeMillis() + 1000 * 60 * expiryMinutesResetPassword);
            case INVITATION_TOKEN -> new Date(System.currentTimeMillis() + 1000 * 60 * expiryDayInvitation * 24 * 60);
            default -> throw new UnauthorizedException("Invalid token type");
        };
    }

    @Override
    public String generateTokenResetPassword(String email) {
        log.info("Generate token reset password for email: {}", email);
        return generateToken(RESET_PASSWORD_TOKEN, null, email);
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
        } catch (ExpiredJwtException | SignatureException | MalformedJwtException e) {
            throw new AccessDeniedException("Access denied!, error: " + e.getMessage());
        }
    }

    private String generateAccessToken(Map<String, Object> claims, String email) {
        log.info("Generate access token for email: {}", email);
        return generateToken(ACCESS_TOKEN, claims, email);
    }

    private String generateRefreshToken(Map<String, Object> claims, String email) {
        log.info("Generate refresh token for email: {}", email);
        return generateToken(REFRESH_TOKEN, claims, email);
    }

    private SecretKey getKey(TokenType type) {
        String key = switch (type) {
            case ACCESS_TOKEN -> accessKey;
            case REFRESH_TOKEN -> refreshKey;
            case RESET_PASSWORD_TOKEN -> resetPasswordKey;
            case CONFIRMATION_TOKEN -> confirmationKey;
            case INVITATION_TOKEN -> invitationKey;
            default -> throw new InvalidDataException("Invalid token type");
        };
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(key));
    }


}
