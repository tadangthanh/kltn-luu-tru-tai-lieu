package vn.kltn.service;

import org.springframework.web.multipart.MultipartFile;
import vn.kltn.dto.response.RepoResponseDto;

public interface IMailService {
    String sendEmail(String recipients, String subject, String content, MultipartFile[] files);

    void sendConfirmLink(String email, Long id, String token);

    void sendForgotPasswordLink(String email, String token);

    void sendAddMemberToRepo(String receiverEmail, RepoResponseDto repo, long expiryDay, String token);
}
