package vn.kltn.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.kltn.service.IUserService;

@RequiredArgsConstructor
@RestController
@Validated
@RequestMapping("/api/v1/user")
@Slf4j(topic = "USER_CONTROLLER")
public class UserController {
    private final IUserService userService;

}
