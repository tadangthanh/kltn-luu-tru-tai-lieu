package vn.kltn.service;

import org.springframework.web.multipart.MultipartFile;
import vn.kltn.entity.Document;
import vn.kltn.entity.Folder;

public interface IMailService {
    String sendEmail(String recipients, String subject, String content, MultipartFile[] files);

    void sendEmailTransferOwnershipDocument(String recipientEmail, Document document);

    void sendEmailTransferOwnershipFolder(String recipientEmail, Folder folder);

    void sendConfirmLink(String email, Long id, String token);

    void sendForgotPasswordLink(String email, String token);

}
