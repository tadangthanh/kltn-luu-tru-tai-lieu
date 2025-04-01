package vn.kltn.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import vn.kltn.dto.response.FolderResponse;
import vn.kltn.entity.Folder;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.map.FolderMapper;
import vn.kltn.repository.FolderRepo;

@Service
@Slf4j(topic = "FOLDER_COMMON_SERVICE")
@RequiredArgsConstructor
public class FolderCommonService {
    private final FolderRepo folderRepo;
    private final FolderMapper folderMapper;


    public Page<Folder> getPageFolderBySpec(Specification<Folder> spec, Pageable pageable) {
        log.info("Get page folder by specification");
        return folderRepo.findAll(spec, pageable);
    }

    public FolderResponse mapToFolderResponse(Folder folder) {
        return folderMapper.toFolderResponse(folder);
    }

    public Folder  getFolderByIdOrThrow(Long folderId) {
        log.info("get folder by id or throw: folderId={}", folderId);
        return folderRepo.findById(folderId).orElseThrow(() -> {
            log.warn("Folder with id {} not found", folderId);
            return new ResourceNotFoundException("Không tìm thấy thư mục");
        });
    }
}
