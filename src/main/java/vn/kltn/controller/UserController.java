package vn.kltn.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.kltn.dto.request.UserRegister;
import vn.kltn.dto.response.ResponseData;
import vn.kltn.service.IUserService;

@RequiredArgsConstructor
@RestController
@Validated
@RequestMapping("/api/v1/user")
@Slf4j(topic = "USER_CONTROLLER")
public class UserController {
    private final IUserService userService;

    @PostMapping("/register")
    public ResponseData<String> register(@Valid @RequestBody UserRegister userRegister) {
        log.info("register user with email: {}", userRegister.getEmail());
        userService.register(userRegister);
        return new ResponseData<>(HttpStatus.CREATED.value(), "Vui lòng kiểm tra email để xác nhận tài khoản");
    }
    @GetMapping("/confirm/{userId}")
    public ResponseData<String> confirmEmail(@PathVariable("userId") Long userId,@RequestParam("token") String token) {
        log.info("confirm email with token: {}", token);
        userService.confirmEmail(userId,token);
        return new ResponseData<>(HttpStatus.OK.value(), "Xác nhận email thành công");
    }
    @PostMapping("/re-confirm")
    public ResponseData<String> reConfirmEmail(@RequestParam("email") String email) {
        log.info("re-confirm email with email: {}", email);
        userService.reConfirmEmail(email);
        return new ResponseData<>(HttpStatus.OK.value(), "Vui lòng kiểm tra email để xác nhận tài khoản");
    }
}
