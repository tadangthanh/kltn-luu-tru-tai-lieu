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

    FileShareResponse shareFile(Long fileId, FileShareRequest fileShareRequest);

    FileDataResponse viewFile(String token, String password);

    void deleteFileShareByFileId(Long fileId);
}
