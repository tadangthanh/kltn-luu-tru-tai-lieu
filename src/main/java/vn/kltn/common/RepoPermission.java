package vn.kltn.common;

public enum RepoPermission {
    // Quyền tạo tài nguyên
    CREATE,  // Tạo blob mới (file mới)

    // Quyền đọc dữ liệu
    READ,    // Đọc nội dung file trong repository

    // Quyền cập nhật dữ liệu (chỉ áp dụng cho Blob, không áp dụng cho Container)
    UPDATE,  // Cập nhật metadata của blob (file)

    // Quyền xóa tài nguyên
    DELETE,  // Xóa blob hoặc container

    // Quyền liệt kê
    LIST,    // Liệt kê tất cả blob trong repository

    // Quyền ghi dữ liệu (cho phép ghi đè file đã có)
    WRITE,   // Ghi dữ liệu vào blob (có thể ghi đè)

    // Quyền thêm mới (chỉ cho phép thêm file, không ghi đè)
    ADD      // Chỉ được thêm file mới vào repository, không ghi đè,
}
