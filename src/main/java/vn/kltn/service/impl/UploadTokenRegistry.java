package vn.kltn.service.impl;

import org.springframework.stereotype.Service;
import vn.kltn.common.CancellationToken;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UploadTokenRegistry {
    // Sử dụng ConcurrentHashMap để đảm bảo an toàn khi truy cập từ nhiều luồng
    private final ConcurrentHashMap<String, CancellationToken> tokens = new ConcurrentHashMap<>();

    // Đăng ký token mới, trả về ID của quá trình upload
    public String register(CancellationToken token) {
        String uploadId = UUID.randomUUID().toString();
        tokens.put(uploadId, token);
        return uploadId;
    }

    // Lấy token tương ứng theo uploadId
    public CancellationToken getToken(String uploadId) {
        return tokens.get(uploadId);
    }

    // Xóa token sau khi upload kết thúc hoặc bị hủy để tránh rò rỉ bộ nhớ
    public void removeToken(String uploadId) {
        tokens.remove(uploadId);
    }
}
