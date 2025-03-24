package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.kltn.entity.Folder;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.repository.FolderRepo;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j(topic = "FOLDER_COMMON_SERVICE")
public class FolderCommonService {
    private final FolderRepo folderRepo;

    public Folder getFolderByIdOrThrow(Long folderId) {
        return folderRepo.findById(folderId).orElseThrow(() -> {
            log.warn("Folder with id {} not found", folderId);
            return new ResourceNotFoundException("Không tìm thấy thư mục");
        });
    }

}
