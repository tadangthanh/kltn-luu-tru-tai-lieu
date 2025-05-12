package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.kltn.entity.Folder;
import vn.kltn.repository.FolderRepo;
import vn.kltn.service.IDocumentService;
import vn.kltn.service.IFolderOperation;
import vn.kltn.service.IItemIndexService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "FOLDER_RESTORATION_SERVICE")
@Transactional
public class RestoreOperation implements IFolderOperation {
    private final FolderRepo folderRepo;
    private final IDocumentService documentService;
    private final IItemIndexService itemIndexService;

    @Override
    public void execute(Folder folder) {
        List<Long> folderIdsRestore = folderRepo.findCurrentAndChildFolderIdsByFolderId(folder.getId());
        folderRepo.setDeleteForFolders(folderIdsRestore, null, null);
        List<Long> folderIds = folderRepo.findCurrentAndChildFolderIdsByFolderId(folder.getId());
        itemIndexService.markDeleteItems(folderIds, false);
        documentService.restoreDocumentsByFolderIds(folderIds);
    }
}
