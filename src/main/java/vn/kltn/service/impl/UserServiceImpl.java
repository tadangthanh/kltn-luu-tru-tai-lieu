package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.kltn.common.TokenType;
import vn.kltn.common.UserStatus;
import vn.kltn.dto.request.AuthChangePassword;
import vn.kltn.dto.request.AuthResetPassword;
import vn.kltn.dto.request.UserRegister;
import vn.kltn.dto.response.TokenResponse;
import vn.kltn.entity.RedisToken;
import vn.kltn.entity.Role;
import vn.kltn.entity.User;
import vn.kltn.exception.*;
import vn.kltn.map.UserMapper;
import vn.kltn.repository.UserRepo;
import vn.kltn.service.*;

import java.util.stream.Collectors;

import static vn.kltn.common.TokenType.ACCESS_TOKEN;
import static vn.kltn.common.TokenType.RESET_PASSWORD_TOKEN;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "USER_SERVICE")
@Transactional
public class UserServiceImpl implements IUserService {
    private final UserRepo userRepo;
    private final IMailService gmailService;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final IRedisTokenService redisTokenService;
    private final IJwtService jwtService;
    private final IRoleService roleService;
    private final IUserHasRoleService userHasRoleService;


    @Override
    public UserDetails loadUserByUsername(String email) {
        return userRepo.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }


    @Override
    public void register(UserRegister userRegister) {
        validationUserRegister(userRegister);
        User user = userMapper.registerToUser(userRegister);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user = userRepo.save(user);
        user.setStatus(UserStatus.NONE);
        Role role = roleService.findRoleByName("user");
        // user tao moi se co role la user
        userHasRoleService.saveUserHasRole(user, role);
        // Gửi email bất đồng bộ
        gmailService.sendConfirmLink(user.getEmail(), user.getId(), jwtService.generateTokenConfirmEmail(user.getEmail()));
    }

    private void validationUserRegister(UserRegister userRegister) {
        if (!isMatchPassword(userRegister.getPassword(), userRegister.getConfirmPassword())) {
            throw new PasswordMismatchException("Mật khẩu không khớp");
        }
        if (userRepo.existsByEmail(userRegister.getEmail())) {
            throw new ConflictResourceException("Email đã được sử dụng");
        }
    }

    private boolean isMatchPassword(String password, String confirmPassword) {
        return password.equals(confirmPassword);
    }

    @Override
    public void confirmEmail(Long userId, String token) {
        User user = findUserByIdOrThrow(userId);
        validateUserActivationStatus(user);
        validateToken(token, TokenType.CONFIRMATION_TOKEN);
        activateUserAccount(user);
    }

    @Override
    public void reConfirmEmail(String email) {
        User user = getUserByEmail(email);
        validateUserActivationStatus(user);
        gmailService.sendConfirmLink(user.getEmail(), user.getId(), jwtService.generateTokenConfirmEmail(user.getEmail()));
    }

    @Override
    public void forgotPassword(String email) {
        if (!userRepo.existsByEmail(email)) {
            throw new ResourceNotFoundException("Email không tồn tại");
        }
        String token = jwtService.generateTokenResetPassword(email);
        redisTokenService.save(RedisToken.builder().id(email).resetToken(token).build());
        gmailService.sendForgotPasswordLink(email, token);
    }

    @Override
    public void resetPassword(AuthResetPassword authResetPassword) {
        String token = authResetPassword.getToken();
        User user = validateTokenForResetPassword(token);
        String passwordNew = authResetPassword.getNewPassword();
        if (!isMatchPassword(passwordNew, authResetPassword.getConfirmPassword())) {
            throw new PasswordMismatchException("Mật khẩu không khớp");
        }
        user.setPassword(passwordEncoder.encode(passwordNew));
        userRepo.save(user);
        redisTokenService.delete(user.getEmail());
    }

    @Override
    public void updatePassword(AuthChangePassword authChangePassword) {
        log.info("update password");
        User currentUser = validateTokenForUpdatePassword(authChangePassword.getToken());
        validateUpdatePassword(authChangePassword, currentUser);
        currentUser.setPassword(passwordEncoder.encode(authChangePassword.getNewPassword()));
        userRepo.save(currentUser);
    }

    @Transactional
    public User createFromGoogle(String email, String fullName, String avatarUrl) {
        // Nếu cần lấy sub từ token thì truyền vào tham số thêm
        User user = new User();
        user.setEmail(email);
        user.setFullName(fullName);
        user.setAvatarUrl(avatarUrl);
        user.setStatus(UserStatus.ACTIVE);

        // Password để trống vì user này login qua Google
        user.setPassword(null);

        userRepo.save(user);
        // Gán role mặc định
        Role userRole = roleService.findRoleByName("user");
        userHasRoleService.saveUserHasRole(user,userRole);
        return user;
    }


    @Override
    public User getUserByEmail(String email) {
        return userRepo.findByEmail(email).orElseThrow(() -> {
            log.warn("User not found by email: {}", email);
            return new ResourceNotFoundException("Không tìm thấy user {}" + email);
        });
    }

    @Override
    public User getUserById(Long id) {
        return userRepo.findById(id).orElseThrow(() -> {
            log.warn("User not found by id: {}", id);
            return new ResourceNotFoundException("Không tìm thấy user {}" + id);
        });
    }



    @Override
    public TokenResponse getTokenResponse(User user) {
        return TokenResponse.builder().accessToken(jwtService.generateAccessToken(user.getId(), user.getEmail(), user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))).refreshToken(jwtService.generateRefreshToken(user.getId(), user.getEmail(), user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))).build();
    }

    @Override
    public User getByEmail(String email) {
        return userRepo.findByEmail(email).orElseThrow(() -> {
            log.error("User not found by email: {}", email);
            return new ResourceNotFoundException("User not found");
        });
    }



    private void validateUpdatePassword(AuthChangePassword authChangePassword, User currentUser) {
        String currentPassword = authChangePassword.getCurrentPassword();
        String newPassword = authChangePassword.getNewPassword();
        if (!passwordEncoder.matches(currentPassword, currentUser.getPassword())) {
            throw new PasswordMismatchException("Mật khẩu hiện tại không đúng");
        }
        if (!isMatchPassword(newPassword, authChangePassword.getConfirmPassword())) {
            throw new PasswordMismatchException("Nhập lại mật khẩu không khớp");
        }
        if (passwordEncoder.matches(newPassword, currentUser.getPassword())) {
            throw new PasswordMismatchException("Mật khẩu mới không được trùng với mật khẩu cũ");
        }
    }

    private void validateUserActivationStatus(User user) {
        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new ConflictResourceException("Tài khoản đã được kích hoạt trước đó!");
        }
    }

    private void validateToken(String token, TokenType type) {
        if (!jwtService.isTokenValid(token, type)) {
            throw new InvalidTokenException("Token không hợp lệ");
        }
    }

    private void activateUserAccount(User user) {
        user.setStatus(UserStatus.ACTIVE);
        userRepo.save(user);
    }


    private User findUserByIdOrThrow(Long id) {
        return userRepo.findById(id).orElseThrow(() -> {
            log.error("User not found, id: {}", id);
            return new ResourceNotFoundException("User not found");
        });
    }

    private User validateTokenForUpdatePassword(String token) {
        // validate token
        var email = jwtService.extractEmail(token, ACCESS_TOKEN);
        // check token in redis
        redisTokenService.isExists(email);

        // validate user is active or not
        var user = userRepo.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found!"));
        if (!user.isEnabled()) {
            throw new InvalidDataException("User not active");
        }
        return user;
    }

    private User validateTokenForResetPassword(String token) {
        // validate token
        var email = jwtService.extractEmail(token, RESET_PASSWORD_TOKEN);
        // check token in redis
        redisTokenService.isExists(email);

        // validate user is active or not
        var user = userRepo.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found!"));
        if (!user.isEnabled()) {
            throw new InvalidDataException("User not active");
        }
        return user;
    }
}
