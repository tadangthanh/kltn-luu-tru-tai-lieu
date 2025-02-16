package vn.kltn.service.impl;

import com.nimbusds.oauth2.sdk.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import vn.kltn.common.TokenType;
import vn.kltn.dto.request.AuthRequest;
import vn.kltn.dto.response.TokenResponse;
import vn.kltn.entity.RedisToken;
import vn.kltn.exception.InvalidDataException;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.exception.UnauthorizedException;
import vn.kltn.repository.UserRepo;
import vn.kltn.service.IAuthenticationService;
import vn.kltn.service.IJwtService;
import vn.kltn.service.IRedisTokenService;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpHeaders.REFERER;
import static vn.kltn.common.TokenType.ACCESS_TOKEN;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "AUTHENTICATION-SERVICE")
public class AuthenticationServiceImpl implements IAuthenticationService {
    private final IJwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserRepo userRepo;
    private final IRedisTokenService redisTokenService;

    @Override
    public TokenResponse getAccessToken(AuthRequest authRequest) {
        log.info("Get access token");
        List<String> authorities = new ArrayList<>();
        try {
            // neu khong co exception thi login thanh cong
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword()));
            log.info("isAuthenticated: {}", authentication.isAuthenticated());
            log.info("Authorities: {}", authentication.getAuthorities().toString());
            // lay ra authorities
            authentication.getAuthorities().forEach(authority -> authorities.add(authority.getAuthority()));
        } catch (AuthenticationException e) {
            log.error("Login fail, message: {}", e.getMessage());
            throw new UnauthorizedException(e.getMessage());
        }
        var user = userRepo.findByEmail(authRequest.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản hoặc mật khẩu không đúng"));
        String accessToken = jwtService.generateAccessToken(user.getId(), authRequest.getEmail(), authorities);
        String refreshToken = jwtService.generateRefreshToken(user.getId(), authRequest.getEmail(), authorities);
        // save token to redis
        redisTokenService.save(RedisToken.builder()
                .id(user.getEmail())
                .accessToken(accessToken)
                .resetToken(refreshToken)
                .build());
        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public TokenResponse getRefreshToken(String token) {
        log.info("Get refresh token");
        String email = jwtService.extractEmail(token, TokenType.REFRESH_TOKEN);
        var user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản không tồn tại"));
        List<String> authorities = new ArrayList<>();
        user.getAuthorities().forEach(authority -> authorities.add(authority.getAuthority()));
        String accessToken = jwtService.generateAccessToken(user.getId(), email, authorities);
        String refreshToken = jwtService.generateRefreshToken(user.getId(), email, authorities);
        // save token to redis
        redisTokenService.save(RedisToken.builder()
                .id(user.getEmail())
                .accessToken(accessToken)
                .resetToken(refreshToken)
                .build());
        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    private String removeToken(HttpServletRequest request) {
        log.info("---------- removeToken ----------");

        final String token = request.getHeader(REFERER);
        if (StringUtils.isBlank(token)) {
            throw new InvalidDataException("Token không được để trống");
        }

        final String userName = jwtService.extractEmail(token, ACCESS_TOKEN);

        // tokenService.delete(userName);
        redisTokenService.delete(userName);

        return "Removed!";
    }

}
