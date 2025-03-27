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
import vn.kltn.common.TokenType;
import vn.kltn.entity.*;
import vn.kltn.service.IJwtService;
import vn.kltn.service.IMailService;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
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
    @Value("${app.link.open-document}")
    private String openDocLink;
    @Value("${app.link.open-folder}")
    private String openFolderLink;
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
    @Value("${spring.mail.invitation-repo-url}")
    private String invitationRepoUrl;
    @Value("${spring.mail.reset-password-url}")
    private String resetPasswordUrl;
    @Value("${jwt.expirationDayInvitation}")
    private long expiryDayInvitation;

    private final IJwtService jwtService;

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
    public void sendEmailInviteDocumentAccess(String recipientEmail, DocumentAccess documentAccess, String message) {
        log.info("sending email invite to: {}", recipientEmail);
        Document document = documentAccess.getResource();
        User owner = document.getOwner();
        String subject = String.format("%s đã chia sẻ một tài liệu với bạn", owner.getFullName());
        String template = "email-invite-document.html";
        Context context = new Context();
        context.setVariable("openDocLink", openDocLink + document.getId());
        context.setVariable("ownerName", owner.getFullName());
        context.setVariable("documentName", document.getName());
        context.setVariable("permission", documentAccess.getPermission().getDescription());
        context.setVariable("message", message);
        sendEmail(owner.getFullName(), recipientEmail, subject, template, context);
    }

    @Override
    @Async
    public void sendEmailInviteFolderAccess(String recipientEmail, FolderAccess folderAccess, String message) {
        log.info("sending email invite to: {}", recipientEmail);
        Folder folder = folderAccess.getResource();
        User owner = folder.getOwner();
        String subject = String.format("%s đã chia sẻ một tài liệu với bạn", owner.getFullName());
        String template = "email-invite-folder.html";
        Context context = new Context();
        context.setVariable("openFolderLink", openFolderLink + folder.getId());
        context.setVariable("ownerName", owner.getFullName());
        context.setVariable("folderName", folder.getName());
        context.setVariable("message", message);
        context.setVariable("permission", folderAccess.getPermission().getDescription());
        sendEmail(owner.getFullName(), recipientEmail, subject, template, context);
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

    @Override
    @Async
    public void sendInvitationMember(String email, Repo repo) {
        log.info("Sending invitation repository to {}", email);
        String subject = "Lời mời tham gia";
        String template = "invitation-repo.html";
        Context context = new Context();
        String token = jwtService.generateToken(TokenType.INVITATION_TOKEN, new HashMap<>(), email);
        context.setVariable("linkAccept", invitationRepoUrl + "/accept?repoId=" + repo.getId() + "&token=" + token);
        context.setVariable("linkReject", invitationRepoUrl + "/reject?repoId=" + repo.getId() + "&email=" + email);
        context.setVariable("repo", repo);
        context.setVariable("owner", repo.getOwner());
        context.setVariable("expiryDayInvitation", expiryDayInvitation);
        sendEmail("Ta dang thanh", email, subject, template, context);

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
