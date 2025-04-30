package vn.kltn.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import vn.kltn.dto.ProcessUploadResult;
import vn.kltn.dto.response.WebSocketMessage;

@Service
@RequiredArgsConstructor
public class WebSocketService {
    private final SimpMessagingTemplate messagingTemplate;

    public void sendUploadSuccess(String userEmail, ProcessUploadResult result) {
        messagingTemplate.convertAndSendToUser(userEmail, "/topic/upload-success", result)
        ;
    }

    public void sendUploadError(String userEmail, String message) {
        messagingTemplate.convertAndSendToUser(userEmail, "/topic/upload-failure", new WebSocketMessage("ERROR", message));
    }
}
