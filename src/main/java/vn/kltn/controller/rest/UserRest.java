package vn.kltn.controller.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.kltn.dto.request.AuthChangePassword;
import vn.kltn.dto.request.UserRegister;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.dto.response.ResponseData;
import vn.kltn.dto.response.UserIndexResponse;
import vn.kltn.index.UserIndex;
import vn.kltn.service.IUserIndexService;
import vn.kltn.service.IUserService;

import java.util.List;

@RequiredArgsConstructor
@RestController
@Validated
@RequestMapping("/api/v1/user")
@Slf4j(topic = "USER_CONTROLLER")
public class UserRest {
    private final IUserService userService;
    private final IUserIndexService userIndexService;

    @PostMapping("/register")
    public ResponseData<String> register(@Validated @RequestBody UserRegister userRegister) {
        log.info("register user with email: {}", userRegister.getEmail());
        userService.register(userRegister);
        return new ResponseData<>(HttpStatus.CREATED.value(), "Vui lòng kiểm tra email để xác nhận tài khoản");
    }

    @GetMapping("/confirm/{userId}")
    public ResponseData<String> confirmEmail(@PathVariable("userId") Long userId, @RequestParam("token") String token) {
        log.info("confirm email with token: {}", token);
        userService.confirmEmail(userId, token);
        return new ResponseData<>(HttpStatus.OK.value(), "Xác nhận email thành công");
    }

    @PostMapping("/re-confirm")
    public ResponseData<String> reConfirmEmail(@RequestParam("email") String email) {
        log.info("re-confirm email with email: {}", email);
        userService.reConfirmEmail(email);
        return new ResponseData<>(HttpStatus.OK.value(), "Vui lòng kiểm tra email để xác nhận tài khoản");
    }

    @PostMapping("/forgot-password")
    public ResponseData<String> forgotPassword(@RequestParam("email") String email) {
        log.info("forgot password with email: {}", email);
        userService.forgotPassword(email);
        return new ResponseData<>(HttpStatus.OK.value(), "Vui lòng kiểm tra email để đổi mật khẩu");
    }

    @PostMapping("/change-password")
    public ResponseData<String> changePassword(@Validated @RequestBody AuthChangePassword authChangePassword) {
        userService.updatePassword(authChangePassword);
        return new ResponseData<>(HttpStatus.OK.value(), "Đổi mật khẩu thành công");
    }

    @GetMapping("/search")
    public ResponseData<PageResponse<List<UserIndexResponse>>> searchUsers(@RequestParam(required = false) String query,
                                                                           Pageable pageable) {

        return new ResponseData<>(200, "thanh cong", userIndexService.searchUsersByEmail(query, pageable));
    }
}
