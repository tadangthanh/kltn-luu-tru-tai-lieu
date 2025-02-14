package vn.kltn.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import vn.kltn.common.TokenType;
import vn.kltn.dto.request.AuthRequest;
import vn.kltn.dto.response.TokenResponse;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.exception.UnauthorizedException;
import vn.kltn.repository.UserRepo;
import vn.kltn.service.IAuthenticationService;
import vn.kltn.service.IJwtService;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "AUTHENTICATION-SERVICE")
public class AuthenticationServiceImpl implements IAuthenticationService {
    private final IJwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserRepo userRepo;

    @Override
    public TokenResponse getAccessToken(AuthRequest authRequest) {
        log.info("Get access token");
        List<String> authorities = new ArrayList<>();
        try {
            // neu khong co exception thi login thanh cong
            Authentication authentication=authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getEmail(),authRequest.getPassword()));
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
        String refreshToken = jwtService.generateRefreshToken(user.getId(),email, authorities);
        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
