package vn.kltn.controller.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @GetMapping("/reset-password")
    public String showResetPasswordPage( @RequestParam("token") String token, Model model) {
        model.addAttribute("token", token); // Gửi token sang Thymeleaf
        return "reset-password-page"; // Trả về template reset-password.html trong thư mục templates
    }
}