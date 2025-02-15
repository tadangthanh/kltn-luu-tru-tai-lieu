package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.kltn.common.TokenType;
import vn.kltn.common.UserStatus;
import vn.kltn.dto.request.AuthResetPassword;
import vn.kltn.dto.request.UserRegister;
import vn.kltn.entity.Role;
import vn.kltn.entity.User;
import vn.kltn.entity.UserHasRole;
import vn.kltn.exception.ConflictResourceException;
import vn.kltn.exception.InvalidTokenException;
import vn.kltn.exception.PasswordMismatchException;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.map.UserMapper;
import vn.kltn.repository.RoleRepo;
import vn.kltn.repository.UserHasRoleRepo;
import vn.kltn.repository.UserRepo;
import vn.kltn.service.IJwtService;
import vn.kltn.service.IMailService;
import vn.kltn.service.IUserService;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "USER_SERVICE")
@Transactional
public class UserServiceImpl implements IUserService {
    private final UserRepo userRepo;
    private final RoleRepo roleRepo;
    private final IMailService gmailService;
    private final UserHasRoleRepo userHasRoleRepo;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final IJwtService jwtService;


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
        Role role = findRoleByName("user");
        // user tao moi se co role la user
        saveUserHasRole(user, role);
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
        User user = findUserByEmailOrThrow(email);
        validateUserActivationStatus(user);
        gmailService.sendConfirmLink(user.getEmail(), user.getId(), jwtService.generateTokenConfirmEmail(user.getEmail()));
    }

    @Override
    public void forgotPassword(String email) {
        if (!userRepo.existsByEmail(email)) {
            throw new ResourceNotFoundException("Email không tồn tại");
        }
        gmailService.sendForgotPasswordLink(email, jwtService.generateTokenResetPassword(email));
    }

    @Override
    public void resetPassword(AuthResetPassword authResetPassword) {
        String token = authResetPassword.getToken();
        validateToken(token, TokenType.RESET_PASSWORD_TOKEN);
        String extractedEmail = jwtService.extractEmail(token, TokenType.RESET_PASSWORD_TOKEN);
        String passwordNew = authResetPassword.getNewPassword();
        if (!isMatchPassword(passwordNew, authResetPassword.getConfirmPassword())) {
            throw new PasswordMismatchException("Mật khẩu không khớp");
        }
        User user = findUserByEmailOrThrow(extractedEmail);
        user.setPassword(passwordEncoder.encode(passwordNew));
        userRepo.save(user);
    }


    private User findUserByEmailOrThrow(String email) {
        return userRepo.findByEmail(email).orElseThrow(() -> {
            log.error("User not found by email: {}", email);
            return new ResourceNotFoundException("User not found");
        });
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


    private UserHasRole saveUserHasRole(User user, Role role) {
        UserHasRole userHasRole = new UserHasRole();
        userHasRole.setUser(user);
        userHasRole.setRole(role);
        return userHasRoleRepo.save(userHasRole);
    }


    private Role findRoleByName(String name) {
        return roleRepo.findRoleByName(name).orElseThrow(() -> {
            log.error("Role not found, name: {}", name);
            return new ResourceNotFoundException("Role not found");
        });
    }

    private User findUserByIdOrThrow(Long id) {
        return userRepo.findById(id).orElseThrow(() -> {
            log.error("User not found, id: {}", id);
            return new ResourceNotFoundException("User not found");
        });
    }
}
