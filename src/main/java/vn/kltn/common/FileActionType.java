package vn.kltn.common;

import lombok.Getter;

@Getter
public enum FileActionType {
    UPLOAD("Tải lên file"),
    DOWNLOAD("Tải file xuống"),
    UPDATE("Chỉnh sửa file"),
    DELETE("Xóa file"),
    RESTORE("Khôi phục file"),
    RENAME("Đổi tên file"),
    MOVE("Di chuyển file"),
    COPY("Sao chép file"),
    SHARE("Chia sẻ file"),
    PERMISSION_CHANGE("Thay đổi quyền truy cập"),
    CHANGE_ACCESS_SCOPE("Thay đổi phạm vi truy cập"),
    VERSION_RESTORE("Khôi phục phiên bản cũ"),
    TAG_ADDED("Thêm tag vào file"),
    TAG_REMOVED("Xóa tag khỏi file");

    private final String description;

    FileActionType(String description) {
        this.description = description;
    }
}
