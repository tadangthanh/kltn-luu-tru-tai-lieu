package vn.kltn.service;

import org.springframework.web.multipart.MultipartFile;
import vn.kltn.dto.request.FileRequest;
import vn.kltn.dto.response.FileResponse;

public interface IFileService {
    FileResponse uploadFile(Long repoId, FileRequest fileRequest,MultipartFile file);
    String calculateChecksum(MultipartFile file);
}
