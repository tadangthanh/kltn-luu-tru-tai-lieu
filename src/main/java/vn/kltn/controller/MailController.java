package vn.kltn.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.kltn.dto.response.ResponseData;
import vn.kltn.service.IMailService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/mail")
@Slf4j(topic = "MAIL_CONTROLLER")
public class MailController {
    private final IMailService gmailService;

    @PostMapping("/send-email")
    public ResponseData<String> send(@RequestParam String recipients, @RequestParam String subject, @RequestParam String content, @RequestParam(required = false) MultipartFile[] files) {
        return new ResponseData<>(HttpStatus.ACCEPTED.value(), gmailService.sendEmail(recipients, subject, content, files));
    }
    @GetMapping("/confirm/{userId}")
    public ResponseData<?> confirmUser(@PathVariable Long userId, @RequestParam(required = false) String secretCode) {
        log.info("confirm user with id: {} to secret: {}", userId, secretCode);
        gmailService.sendConfirmLink("tathanh203lnbg@gmail.com", userId,"123456");
        return new ResponseData<>(HttpStatus.OK.value(), "confirm success");
    }
}
