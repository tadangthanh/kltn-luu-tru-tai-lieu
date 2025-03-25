package vn.kltn.service;

import org.springframework.web.multipart.MultipartFile;
import vn.kltn.entity.DocumentAccess;
import vn.kltn.entity.FolderAccess;
import vn.kltn.entity.Repo;

public interface IMailService {
    String sendEmail(String recipients, String subject, String content, MultipartFile[] files);

    void sendEmailInviteDocumentAccess(String recipientEmail, DocumentAccess documentAccess);

    void sendEmailInviteFolderAccess(String recipientEmail, FolderAccess folderAccess);

    void sendConfirmLink(String email, Long id, String token);

    void sendForgotPasswordLink(String email, String token);

    void sendInvitationMember(String receiverEmail, Repo repo);
}
