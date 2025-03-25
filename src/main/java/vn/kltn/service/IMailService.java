package vn.kltn.service;

import org.springframework.web.multipart.MultipartFile;
import vn.kltn.entity.*;

public interface IMailService {
    String sendEmail(String recipients, String subject, String content, MultipartFile[] files);

    void sendEmailInviteDocumentAccess(String recipientEmail, DocumentAccess documentAccess, String message);

    void sendEmailInviteFolderAccess(String recipientEmail, FolderAccess folderAccess, String message);

    void sendEmailTransferOwnershipDocument(String recipientEmail, Document document);

    void sendEmailTransferOwnershipFolder(String recipientEmail, Folder folder);

    void sendConfirmLink(String email, Long id, String token);

    void sendForgotPasswordLink(String email, String token);

    void sendInvitationMember(String receiverEmail, Repo repo);
}
