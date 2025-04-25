package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.kltn.entity.Folder;
import vn.kltn.repository.FolderRepo;
import vn.kltn.service.IDocumentCommonService;
import vn.kltn.service.IFolderDeletionService;
import vn.kltn.service.IFolderOperation;
import vn.kltn.service.IFolderValidation;

@Service
@Transactional
@Slf4j(topic = "FOLDER_DELETION_SERVICE")
@RequiredArgsConstructor
public class FolderDeletionServiceImpl implements IFolderDeletionService {
    private final FolderRepo folderRepo;
    private final IFolderOperation softDeleteOperation;
    private final IFolderOperation hardDeleteOperation;
    private final IDocumentCommonService documentService;
    private final IFolderValidation folderValidation;

    @Override
    public void softDelete(Folder folder) {
        log.info("Soft delete folder: folderId={}", folder.getId());
        softDeleteOperation.execute(folder);
    }

    @Override
    public void hardDelete(Folder folder) {
        log.info("Hard delete folder: folderId={}", folder.getId());
        folderValidation.validateFolderDeleted(folder);
        hardDeleteOperation.execute(folder);
    }


}
