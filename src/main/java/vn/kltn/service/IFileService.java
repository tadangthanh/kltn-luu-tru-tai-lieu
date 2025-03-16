package vn.kltn.service;

import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import vn.kltn.dto.request.FileRequest;
import vn.kltn.dto.request.FileShareRequest;
import vn.kltn.dto.response.FileDataResponse;
import vn.kltn.dto.response.FileResponse;
import vn.kltn.dto.response.FileShareResponse;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.entity.File;

import java.time.LocalDate;
import java.util.List;

public interface IFileService {
    FileResponse uploadFile(Long repoId, FileRequest fileRequest, MultipartFile file);

    String calculateChecksumHexFromFile(MultipartFile file);

    String calculateChecksumHexFromFileByte(byte[] data);

    void validateFileIntegrity(File file); // kiem tra tinh toan ven cua file

    void deleteFile(Long fileId);

    FileResponse restoreFile(Long fileId);

    File getFileById(Long fileId);

    Long getRepoIdByFileId(Long fileId);

    FileResponse updateFileMetadata(Long fileId, FileRequest fileRequest);

    FileDataResponse downloadFile(Long fileId);

    PageResponse<List<FileResponse>> advanceSearchBySpecification(Long repoId, Pageable pageable, String[] file);

    PageResponse<List<FileResponse>> searchByTagName(Long repoId, String tagName, Pageable pageable);

    PageResponse<List<FileResponse>> searchByStartDateAndEndDate(Long repoId, Pageable pageable, LocalDate startDate, LocalDate endDate);

    FileShareResponse createFileShareLink(Long fileId, FileShareRequest fileShareRequest);

    FileDataResponse viewFile(String token, String password);

    void deleteFileShareById(Long id);
//    Dùng public key để giải mã chữ ký số, lấy lại giá trị hash ban đầu.
//    Băm file tải xuống bằng cùng thuật toán (ví dụ: SHA-256) để tạo hash mới.
//    So sánh hash từ chữ ký với hash mới tính từ file:
//    Nếu khớp: file hợp lệ (không bị thay đổi, và đúng nguồn gốc).
//    Nếu không khớp: file đã bị thay đổi hoặc không đúng nguồn gốc.

}
