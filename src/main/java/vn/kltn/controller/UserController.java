package vn.kltn.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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

}
