package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.kltn.entity.Folder;
import vn.kltn.repository.FolderRepo;
import vn.kltn.service.IFolderOperation;
import vn.kltn.service.IFolderRestorationService;
import vn.kltn.service.IFolderValidation;

@Service
@Slf4j(topic = "FOLDER_RESTORATION_SERVICE")
@RequiredArgsConstructor
@Transactional
public class FolderRestorationServiceImpl implements IFolderRestorationService {
    private final FolderRepo folderRepo;
    private final FolderCommonService folderCommonService;
    private final IFolderValidation folderValidation;
    private final IFolderOperation restoreOperation;
    @Override
    public Folder restore(Long folderId) {
        log.info("Restore folder: folderId={}", folderId);
        Folder folder = folderCommonService.getFolderByIdOrThrow(folderId);
        folderValidation.validateFolderDeleted(folder);
        restoreOperation.execute(folder);
        return folder;
    }
}
