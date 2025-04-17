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
import vn.kltn.entity.Document;
import vn.kltn.entity.Folder;
import vn.kltn.entity.User;
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
    @Value("${app.link.accept-owner-document}")
    private String acceptOwnerDocumentLink;
    @Value("${app.link.accept-owner-folder}")
    private String acceptOwnerFolderLink;
    @Value("${app.link.decline-owner-document}")
    private String declineOwnerDocumentLink;
    @Value("${app.link.decline-owner-folder}")
    private String declineOwnerFolderLink;
    @Value("${app.link.oauth2.google}")
    private String oauth2GoogleLink;
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

        sendEmail("Ta dang thanh", email, subject, template, context);
    }

    @Override
    @Async
    public void sendForgotPasswordLink(String email, String token) {
        log.info("Sending reset password link to {}", email);
        String subject = "Thay đổi mật khẩu";
        String template = "forgot-password-email.html";

        Context context = new Context();
        context.setVariable("linkResetPassword", resetPasswordUrl + "?token=" + token);

        sendEmail("Ta dang thanh", email, subject, template, context);
    }

    @Override
    @Async
    public void sendEmailTransferOwnershipDocument(String recipientEmail, Document document) {
        log.info("sending email transfer ownership document to: {}", recipientEmail);
        User owner = document.getOwner();
        String subject = String.format("Lời mời sở hữu:\"%s \"", document.getName());
        String template = "email-transfer-owner-document.html";
        Context context = new Context();
        context.setVariable("ownerName", owner.getFullName());
        context.setVariable("documentName", document.getName());
        context.setVariable("acceptLink", oauth2GoogleLink + "?redirectUrl=" + acceptOwnerDocumentLink + document.getId());
        context.setVariable("declineLink", oauth2GoogleLink + "?redirectUrl=" + declineOwnerDocumentLink + document.getId());
        sendEmail(owner.getFullName(), recipientEmail, subject, template, context);
    }

    @Override
    @Async
    public void sendEmailTransferOwnershipFolder(String recipientEmail, Folder folder) {
        log.info("sending email transfer ownership folder to: {}", recipientEmail);
        User owner = folder.getOwner();
        String subject = String.format("Lời mời sở hữu:\"%s \"", folder.getName());
        String template = "email-transfer-owner-folder.html";
        Context context = new Context();
        context.setVariable("ownerName", owner.getFullName());
        context.setVariable("folderName", folder.getName());
        context.setVariable("acceptLink", oauth2GoogleLink + "?redirectUrl=" + acceptOwnerFolderLink + folder.getId());
        context.setVariable("declineLink", oauth2GoogleLink + "?redirectUrl=" + declineOwnerFolderLink + folder.getId());
        sendEmail(owner.getFullName(), recipientEmail, subject, template, context);
    }

    private void sendEmail(String from, String recipient, String subject, String template, Context context) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, "UTF-8");

            helper.setFrom(emailFrom, from);
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
