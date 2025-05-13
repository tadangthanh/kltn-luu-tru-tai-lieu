package vn.kltn.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import vn.kltn.dto.ProcessDocUploadResult;
import vn.kltn.dto.response.ItemResponse;
import vn.kltn.dto.response.WebSocketMessage;

@Service
@RequiredArgsConstructor
public class WebSocketService {
    private final SimpMessagingTemplate messagingTemplate;

    public void sendDocUploadSuccess(String userEmail, ProcessDocUploadResult result) {
        messagingTemplate.convertAndSendToUser(userEmail, "/topic/upload-success", result)
        ;
    }
    public void sendFolderUploadSuccess(String userEmail, ItemResponse result) {
        messagingTemplate.convertAndSendToUser(userEmail, "/topic/upload-folder-success", result)
        ;
    }

    public void sendUploadError(String userEmail, String message) {
        messagingTemplate.convertAndSendToUser(userEmail, "/topic/upload-failure", new WebSocketMessage("ERROR", message));
    }
}
