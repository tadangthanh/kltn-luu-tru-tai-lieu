package vn.kltn.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.kltn.dto.request.AuthRequest;
import vn.kltn.dto.response.ResponseData;
import vn.kltn.dto.response.TokenResponse;
import vn.kltn.service.IAuthenticationService;

@RequestMapping("/auth")
@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthenticationController {
    private final IAuthenticationService authenticationService;
    @PostMapping("/access")
    public ResponseData<TokenResponse> login(@Validated @RequestBody AuthRequest authRequest) {
        return new ResponseData<>(HttpStatus.OK.value(), "Login success", authenticationService.login(authRequest));
    }
}
