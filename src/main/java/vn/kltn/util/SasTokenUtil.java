package vn.kltn.util;

import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class SasTokenUtil {
    public static boolean isSasTokenValid(String sasToken) {
        if (sasToken == null || !sasToken.startsWith("?")) {
            return false; // Không phải SAS token hợp lệ
        }

        // Loại bỏ ký tự "?" đầu tiên và tách các tham số
        String[] params = sasToken.substring(1).split("&");
        Map<String, String> tokenParams = new HashMap<>();
        for (String param : params) {
            String[] keyValue = param.split("=", 2);
            if (keyValue.length == 2) {
                tokenParams.put(keyValue[0], keyValue[1]);
            }
        }

        // Lấy start time và expiry time
        String startTimeStr = tokenParams.get("st");
        String expiryTimeStr = tokenParams.get("se");

        if (startTimeStr == null || expiryTimeStr == null) {
            return false; // Thiếu thông tin thời gian
        }

        try {
            // Chuyển đổi chuỗi thời gian thành OffsetDateTime
            DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
            OffsetDateTime startTime = OffsetDateTime.parse(startTimeStr, formatter);
            OffsetDateTime expiryTime = OffsetDateTime.parse(expiryTimeStr, formatter);
            OffsetDateTime now = OffsetDateTime.now();

            // Kiểm tra xem thời gian hiện tại có nằm trong khoảng startTime và expiryTime không
            return now.isAfter(startTime) && now.isBefore(expiryTime);
        } catch (Exception e) {
            return false; // Lỗi định dạng thời gian
        }
    }
}
