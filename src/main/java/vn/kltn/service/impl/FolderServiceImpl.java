package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.kltn.dto.request.FolderRequest;
import vn.kltn.dto.response.FolderResponse;
import vn.kltn.entity.Folder;
import vn.kltn.exception.ConflictResourceException;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.map.FolderMapper;
import vn.kltn.repository.FolderRepo;
import vn.kltn.service.IFolderService;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j(topic = "FOLDER_SERVICE")
public class FolderServiceImpl implements IFolderService {
    private final FolderMapper folderMapper;
    private final FolderRepo folderRepo;

    @Override
    public FolderResponse createFolder(FolderRequest folderRequest) {
        Folder folder = mapToFolder(folderRequest);
        folder = folderRepo.save(folder);
        return mapToFolderResponse(folder);
    }

    private Folder mapToFolder(FolderRequest folderRequest) {
        return folderMapper.toFolder(folderRequest);
    }

    private FolderResponse mapToFolderResponse(Folder folder) {
        return folderMapper.toFolderResponse(folder);
    }

    private Folder getFolderByIdOrThrow(Long folderId) {
        return folderRepo.findById(folderId).orElseThrow(() -> {
            log.warn("Folder with id {} not found", folderId);
            return new ResourceNotFoundException("Không tìm thấy thư mục");
        });
    }

    @Override
    public void deleteFolderById(Long folderId) {
        Folder folder = getFolderByIdOrThrow(folderId);
        validateFolderNotDeleted(folder);
        deleteFolder(folder);
    }

    private void deleteFolder(Folder folder) {
        folder.setDeletedAt(LocalDateTime.now());
        folderRepo.save(folder);
    }

    private void validateFolderNotDeleted(Folder folder) {
        if (folder.getDeletedAt() != null) {
            log.warn("Folder with id {} is already deleted", folder.getId());
            throw new ConflictResourceException("Thư mục đã bị xóa");
        }
    }

    @Override
    public FolderResponse updateFolderById(Long folderId, FolderRequest folderRequest) {
        Folder folder = getFolderByIdOrThrow(folderId);
        folderMapper.updateFolderFromRequest(folderRequest, folder);
        return mapToFolderResponse(folderRepo.save(folder));
    }
}
