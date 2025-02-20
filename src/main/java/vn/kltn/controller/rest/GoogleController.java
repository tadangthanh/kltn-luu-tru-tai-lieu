package vn.kltn.controller.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.kltn.service.IGoogleService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/google")
public class GoogleController {
    private final IGoogleService googleService;

    @PostMapping("/generate-content")
    public String generateContent(@RequestParam("content") String content) {
        return googleService.generateContent(content);
    }
}
