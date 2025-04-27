package vn.kltn.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.kltn.entity.Folder;
import vn.kltn.repository.FolderRepo;
import vn.kltn.service.IDocumentService;
import vn.kltn.service.IFolderOperation;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SoftDeleteOperation implements IFolderOperation {
    private final FolderRepo folderRepo;
    private final IDocumentService documentService;
    @Value("${app.delete.document-retention-days}")
    private int documentRetentionDays;

    @Override
    public void execute(Folder folder) {
        // lay danh sach id cac folder va cac folder con can xoa
        List<Long> folderIdsDelete = folderRepo.findCurrentAndChildFolderIdsByFolderId(folder.getId());
        // update deletedAt cho cac folder va cac folder con
        folderRepo.setDeleteForFolders(folderIdsDelete, LocalDateTime.now(), LocalDateTime.now().plusDays(documentRetentionDays));
        // xoa document cua cac folder va cac folder con
        documentService.softDeleteDocumentsByFolderIds(folderIdsDelete);
    }
}
