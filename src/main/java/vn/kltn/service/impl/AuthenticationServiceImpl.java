package vn.kltn.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import vn.kltn.dto.request.AuthRequest;
import vn.kltn.dto.response.TokenResponse;
import vn.kltn.entity.User;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.repository.UserRepo;
import vn.kltn.service.IAuthenticationService;
import vn.kltn.service.IUserService;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements IAuthenticationService {
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserRepo userRepo;
    @Override
    public TokenResponse login(AuthRequest authRequest) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword()));
            User user = userRepo.findByEmail(authRequest.getEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("Tài khoản hoặc mật khẩu không đúng"));
            String accessToken = jwtService.generateToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);
            return TokenResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .userId(user.getId())
                    .fullName(user.getFullName())
                    .build();
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new ResourceNotFoundException("Tài khoản hoặc mật khẩu không đúng");
        }
    }
}
