package vn.kltn.service;

import org.springframework.web.multipart.MultipartFile;
import vn.kltn.dto.request.FileRequest;
import vn.kltn.dto.response.FileResponse;
import vn.kltn.entity.File;

public interface IFileService {
    FileResponse uploadFile(Long repoId, FileRequest fileRequest, MultipartFile file);

    String calculateChecksum(MultipartFile file);

    void deleteFile(Long fileId);

    File getFileById(Long fileId);
    Long getRepoIdByFileId(Long fileId);
}
