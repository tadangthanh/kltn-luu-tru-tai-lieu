package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.kltn.entity.File;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.repository.FileRepo;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j(topic = "FILE_COMMON_SERVICE")
public class FileCommonService {
    private final FileRepo fileRepo;

    public File getFileById(Long fileId) {
        return fileRepo.findById(fileId).orElseThrow(() -> {
            log.warn("Không tìm thấy file với id: {}", fileId);
            return new ResourceNotFoundException("File không tồn tại");
        });
    }
}
