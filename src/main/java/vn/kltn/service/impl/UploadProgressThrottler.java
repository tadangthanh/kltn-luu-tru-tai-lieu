package vn.kltn.service.impl;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import vn.kltn.dto.response.UploadProgressDTO;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UploadProgressThrottler {
    private final SimpMessagingTemplate messagingTemplate;
    private final long THROTTLE_INTERVAL_MS = 300;
    private final Map<String, Long> lastSentTime = new ConcurrentHashMap<>();

    public UploadProgressThrottler(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendProgress(String email, UploadProgressDTO dto) {
        long now = System.currentTimeMillis();
        String key = email + "|" + dto.getFileName();

        Long lastTime = lastSentTime.getOrDefault(key, 0L);
        if (now - lastTime >= THROTTLE_INTERVAL_MS) {
            lastSentTime.put(key, now);
            messagingTemplate.convertAndSendToUser(email, "/topic/upload-documents", dto);
        }
    }

    public void clear(String email, String fileName) {
        lastSentTime.remove(email + "|" + fileName);
    }
}
