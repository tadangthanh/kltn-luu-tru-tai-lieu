package vn.kltn.util;

import java.text.Normalizer;

public class TextUtils {
    public static String normalizeFileName(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "") // bỏ dấu
                .replaceAll("[^a-zA-Z0-9\\.\\-]", "-") // thay ký tự đặc biệt bằng "-"
                .toLowerCase();
    }

}
