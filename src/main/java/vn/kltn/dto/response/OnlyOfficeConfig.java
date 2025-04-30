package vn.kltn.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OnlyOfficeConfig {
    private Long documentId;
    private String documentKey;
    private String documentTitle;
    private String fileType;
    private String documentType;
    private String documentUrl;
    private String callbackUrl;

    // Thông tin quyền của người dùng
    private Permissions permissions;

    // Thông tin người dùng
    private User user;

    // Nested class để lưu quyền
    @Getter
    @Setter
    public static class Permissions {
        private boolean edit;
        private boolean comment;
        private boolean download;
    }

    // Nested class để lưu thông tin người dùng
    @Getter
    @Setter
    public static class User {
        private String id;
        private String name;
    }
}
