package vn.kltn.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import vn.kltn.dto.request.AuthRequest;
import vn.kltn.dto.response.TokenResponse;
import vn.kltn.entity.User;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.repository.UserRepo;
import vn.kltn.service.IAuthenticationService;
import vn.kltn.service.IJwtService;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "AUTHENTICATION-SERVICE")
public class AuthenticationServiceImpl implements IAuthenticationService {
    private final IJwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserRepo userRepo;

    @Override
    public TokenResponse getAccessToken(AuthRequest authRequest) {
//        try {
//            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword()));
//            User user = userRepo.findByEmail(authRequest.getEmail())
//                    .orElseThrow(() -> new ResourceNotFoundException("Tài khoản hoặc mật khẩu không đúng"));
//            String accessToken = jwtServiceImpl.generateToken(user);
//            String refreshToken = jwtServiceImpl.generateRefreshToken(user);
//            return TokenResponse.builder()
//                    .accessToken(accessToken)
//                    .refreshToken(refreshToken)
//                    .build();
//        } catch (Exception e) {
//            log.error(e.getMessage());
//            throw new ResourceNotFoundException("Tài khoản hoặc mật khẩu không đúng");
//        }
        log.info("Get access token");

        try {
            Authentication authentication=authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getEmail()
                    , authRequest.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (AuthenticationException e) {
            log.error("Login fail, message: {}", e.getMessage());
            throw new AccessDeniedException(e.getMessage());
        }
        var user = userRepo.findByEmail(authRequest.getEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("Tài khoản hoặc mật khẩu không đúng"));
        String accessToken = jwtService.generateAccessToken(user.getId(), authRequest.getEmail(), user.getAuthorities());
        String refreshToken = jwtService.generateRefreshToken(user.getId(), authRequest.getEmail(), user.getAuthorities());
        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public TokenResponse getRefreshToken(String refreshToken) {
        return null;
    }
}
