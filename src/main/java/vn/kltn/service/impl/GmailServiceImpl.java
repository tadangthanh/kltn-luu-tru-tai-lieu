package vn.kltn.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import vn.kltn.service.IMailService;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "GMAIL_SERVICE")
public class GmailServiceImpl implements IMailService {
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine springTemplateEngine;
    @Value("${spring.mail.from}")
    private String emailFrom;
    @Value("${spring.mail.confirm-url}")
    private String confirmUrl;
    @Value("${spring.mail.reset-password-url}")
    private String resetPasswordUrl;


    @Override
    public String sendEmail(String recipients, String subject, String content, MultipartFile[] files) {
        log.info("sending email to: {}, subject: {}", recipients, subject);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(emailFrom, "Ta Dang Thanh");

            if (recipients.contains(",")) {
                helper.setTo(InternetAddress.parse(recipients));
            } else {
                helper.setTo(recipients);
            }
            if (files != null) {
                for (MultipartFile file : files) {
                    helper.addAttachment(Objects.requireNonNull(file.getOriginalFilename()), file);
                }
            }
            helper.setSubject(subject);
            helper.setText(content, true);
            mailSender.send(message);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        log.info("sent ,recipients: {}, subject: {}", recipients, subject);
        return "Email sent successfully";
    }

    @Override
    @Async
    public void sendConfirmLink(String email, Long id, String token) {
        log.info("sending confirm link to {}", email);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, "UTF-8");
            Context context = new Context();

            String linkConfirm = String.format(confirmUrl + "/%s?token=%s", id, token);
            Map<String, Object> properties = Map.of("linkConfirm", linkConfirm);
            context.setVariables(properties);

            helper.setFrom(emailFrom, "Ta Dang Thanh");
            helper.setTo(email);
            helper.setSubject("Kích hoạt tài khoản");
            String html = springTemplateEngine.process("confirm-email.html", context);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        log.info("sent confirm link to {} success", email);
    }

    @Override
    @Async
    public void sendForgotPasswordLink(String email, String token) {
        log.info("sending reset password link to {}", email);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, "UTF-8");
            Context context = new Context();

            String linkResetPassword = String.format(resetPasswordUrl + "?token=%s",token);
            Map<String, Object> properties = Map.of("linkResetPassword", linkResetPassword);
            context.setVariables(properties);
            helper.setFrom(emailFrom, "Ta Dang Thanh");
            helper.setTo(email);
            helper.setSubject("Thay đổi mật khẩu");
            String html = springTemplateEngine.process("forgot-password-email.html", context);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        log.info("sent reset password link to {} success", email);
    }


}
