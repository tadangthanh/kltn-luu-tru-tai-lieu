package vn.kltn.controller.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.kltn.dto.request.AuthRequest;
import vn.kltn.dto.request.AuthResetPassword;
import vn.kltn.dto.response.ResponseData;
import vn.kltn.dto.response.TokenResponse;
import vn.kltn.service.IAuthenticationService;
import vn.kltn.service.IUserService;

import java.io.ByteArrayInputStream;
import java.util.Map;

@RequestMapping("/api/auth")
@Slf4j(topic = "AUTHENTICATION_CONTROLLER")
@RestController
@RequiredArgsConstructor
@Validated
public class AuthenticationRest {
    private final IAuthenticationService authenticationService;
    private final IUserService userService;

    @PostMapping("/access")
    public ResponseData<TokenResponse> login(@Validated @RequestBody AuthRequest authRequest) {
        return new ResponseData<>(HttpStatus.OK.value(), "Login success", authenticationService.getAccessToken(authRequest));
    }

    @PostMapping("/refresh-token")
    public ResponseData<TokenResponse> refreshToken(@RequestHeader("Referer") String refreshToken) {
        return new ResponseData<>(HttpStatus.OK.value(), "Success", authenticationService.getRefreshToken(refreshToken));
    }

    @PostMapping("/reset-password")
    public ResponseData<Void> resetPassword(@Validated @RequestBody AuthResetPassword authResetPassword) {
        userService.resetPassword(authResetPassword);
        return new ResponseData<>(HttpStatus.OK.value(), "Đổi mật khẩu thành công");
    }

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> request) {
        String idToken = request.get("token");
        TokenResponse tokens = authenticationService.verifyGoogleTokenAndLogin(idToken);
        return ResponseEntity.ok(tokens);
    }
}
