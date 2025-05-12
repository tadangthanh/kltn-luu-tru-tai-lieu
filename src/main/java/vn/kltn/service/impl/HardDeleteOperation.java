package vn.kltn.service.impl;

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
@Slf4j(topic = "FOLDER_HARD_DELETE_OPERATION")
public class HardDeleteOperation implements IFolderOperation {
    private final FolderRepo folderRepo;
    private final IDocumentService documentService;
    private final IItemIndexService itemIndexService;
    @Override
    public void execute(Folder folder) {
        log.info("Hard delete folder: folderId={}", folder.getId());
        List<Long> folderIdsToDelete=deleteDocumentChild(folder);
        itemIndexService.deleteIndexByIdList(folderIdsToDelete);
        folderRepo.delete(folder);
    }
    private List<Long> deleteDocumentChild(Folder folder) {
        List<Long> folderIdsDelete = folderRepo.findCurrentAndChildFolderIdsByFolderId(folder.getId());
        documentService.hardDeleteDocumentByFolderIds(folderIdsDelete);
        return folderIdsDelete;
    }
}
