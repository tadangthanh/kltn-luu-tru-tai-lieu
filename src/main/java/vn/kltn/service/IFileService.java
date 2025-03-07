package vn.kltn.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import vn.kltn.dto.request.FileRequest;
import vn.kltn.dto.response.FileDataResponse;
import vn.kltn.dto.response.FileResponse;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.entity.File;

import java.util.List;

public interface IFileService {
    FileResponse uploadFile(Long repoId, FileRequest fileRequest, MultipartFile file);

    String calculateChecksumHexFromFile(MultipartFile file);

    String calculateChecksumHexFromFileByte(byte[] data);

    void validateIntegrity(File file); // kiem tra tinh toan ven cua file

    void deleteFile(Long fileId);

    FileResponse restoreFile(Long fileId);

    File getFileById(Long fileId);

    Long getRepoIdByFileId(Long fileId);

    FileResponse updateFileMetadata(Long fileId, FileRequest fileRequest);

    FileDataResponse downloadFile(Long fileId);

    PageResponse<List<FileResponse>> advanceSearchBySpecification(Pageable pageable, String[] file);

    PageResponse<List<FileResponse>> convertToPageResponse(Page<File> filePage, Pageable pageable);

//    Dùng public key để giải mã chữ ký số, lấy lại giá trị hash ban đầu.
//    Băm file tải xuống bằng cùng thuật toán (ví dụ: SHA-256) để tạo hash mới.
//    So sánh hash từ chữ ký với hash mới tính từ file:
//    Nếu khớp: file hợp lệ (không bị thay đổi, và đúng nguồn gốc).
//    Nếu không khớp: file đã bị thay đổi hoặc không đúng nguồn gốc.


//    Versioning cho File
//
//    Mô tả: Lưu lại các phiên bản của file khi người dùng cập nhật.
//    Lợi ích: Cho phép người dùng xem lịch sử thay đổi, so sánh và phục hồi phiên bản cũ nếu cần.
//    Phân Loại và Gắn Tag
//
//    Mô tả: Cho phép người dùng gắn tag và phân loại file, repository theo chủ đề hoặc loại tài liệu.
//    Lợi ích: Tăng cường khả năng tìm kiếm và quản lý file theo chủ đề hoặc dự án.
//    Bình Luận và Thảo Luận
//
//    Mô tả: Tích hợp hệ thống bình luận trực tiếp trong file hoặc repository, nơi thành viên có thể trao đổi, ghi chú hay phản hồi.
//    Lợi ích: Hỗ trợ làm việc nhóm, tăng cường giao tiếp giữa các thành viên.
//    Thống Kê và Báo Cáo Hoạt Động
//
//    Mô tả: Cung cấp bảng điều khiển thống kê số lượt xem, tải về, lượt chỉnh sửa, và hoạt động của thành viên trên từng repository hoặc file.
//    Lợi ích: Giúp quản trị viên và chủ sở hữu repository nắm bắt được mức độ sử dụng và tương tác của tài liệu.
//    Tìm Kiếm Nâng Cao
//
//    Mô tả: Phát triển tính năng tìm kiếm theo nội dung, tên file, tag, hoặc metadata, giúp người dùng nhanh chóng định vị tài liệu cần thiết.
//    Lợi ích: Nâng cao trải nghiệm người dùng, đặc biệt khi số lượng file và repository lớn.
//    Xem Trước File Trực Tuyến
//
//    Mô tả: Cho phép người dùng xem trước nội dung file (PDF, hình ảnh, văn bản) mà không cần tải về.
//    Lợi ích: Tiết kiệm thời gian và băng thông, tăng tính tiện dụng khi duyệt file.
//    Quản Lý Quyền Truy Cập Chi Tiết
//
//    Mô tả: Cung cấp các mức quyền truy cập chi tiết hơn cho thành viên (xem, chỉnh sửa, xóa, chia sẻ file, quản lý thành viên trong repo…).
//    Lợi ích: Tăng cường bảo mật và kiểm soát, cho phép chủ repository thiết lập chính xác quyền hạn của từng thành viên.
//    Đồng Bộ Hóa với Các Dịch Vụ Khác
//
//    Mô tả: Tích hợp API để đồng bộ file với các dịch vụ đám mây khác như Google Drive, Dropbox, OneDrive.
//    Lợi ích: Giúp người dùng dễ dàng chuyển đổi hoặc sao lưu tài liệu giữa các hệ thống khác nhau.
}
