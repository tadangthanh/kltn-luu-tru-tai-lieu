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
import vn.kltn.dto.response.RepoResponseDto;
import vn.kltn.service.IMailService;

import java.io.UnsupportedEncodingException;
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
    @Value("${spring.mail.invitation-repo-url}")
    private String invitationRepoUrl;
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
        log.info("Sending confirm link to {}", email);
        String subject = "Kích hoạt tài khoản";
        String template = "confirm-email.html";

        Context context = new Context();
        context.setVariable("linkConfirm", confirmUrl + "?token=" + token);

        sendEmail(email, subject, template, context);
    }

    @Override
    @Async
    public void sendForgotPasswordLink(String email, String token) {
        log.info("Sending reset password link to {}", email);
        String subject = "Thay đổi mật khẩu";
        String template = "forgot-password-email.html";

        Context context = new Context();
        context.setVariable("linkResetPassword", resetPasswordUrl + "?token=" + token);

        sendEmail(email, subject, template, context);
    }

    @Override
    @Async
    public void sendAddMemberToRepo(String email, RepoResponseDto repo, long expiryDayInvitation, String token) {
        log.info("Sending invitation repository to {}", email);
        String subject = "Lời mời tham gia";
        String template = "invitation-repo.html";
        Context context = new Context();
        context.setVariable("linkAccept", invitationRepoUrl + "/accept?repoId=" + repo.getId() + "&token=" + token);
        context.setVariable("linkReject", invitationRepoUrl + "/reject?repoId=" + repo.getId() + "&email=" + email);
        context.setVariable("repo", repo);
        context.setVariable("expiryDayInvitation", expiryDayInvitation);
        sendEmail(email, subject, template, context);

    }

    private void sendEmail(String recipient, String subject, String template, Context context) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, "UTF-8");

            helper.setFrom(emailFrom, "Ta Dang Thanh");
            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setText(springTemplateEngine.process(template, context), true);

            mailSender.send(message);
            log.info("Email sent to {} successfully", recipient);
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("Failed to send email to {}", recipient, e);
            throw new RuntimeException("Email sending failed", e);
        }
    }


}
