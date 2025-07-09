package vn.kltn.controller.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.kltn.entity.User;
import vn.kltn.service.IUserService;

import java.util.List;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class Test {
    private final IUserService userService;


    @GetMapping("/all")
    public List<User> getAllUser() {
        return  userService.getAllUser();
    }

}
