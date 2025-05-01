package vn.kltn.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.kltn.entity.Document;
import vn.kltn.entity.Folder;
import vn.kltn.repository.DocumentRepo;
import vn.kltn.repository.FolderRepo;
import vn.kltn.service.IDocumentService;
import vn.kltn.service.IFolderService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "FOLDER_CLEANUP_SERVICE")
public class ItemCleanUpService {
    private final IFolderService folderService;
    private final FolderRepo folderRepo;
    private final DocumentRepo documentRepo;
    private final IDocumentService documentService;

    @Transactional
    @Scheduled(cron = "0 0 * * * *") // chạy mỗi giờ
    public void cleanupExpiredFolders() {
        log.info("start cleanup expired Folders");
        LocalDateTime now = LocalDateTime.now();
        List<Folder> expiredFolders = folderRepo.findAllByPermanentDeleteAtBefore(now);
        for (Folder expiredFolder : expiredFolders) {
            folderService.hardDeleteFolderById(expiredFolder.getId());
        }
        log.info("end cleanup expired Folders");
    }

    @Transactional
    @Scheduled(cron = "0 0 * * * *") // chạy mỗi giờ
    public void cleanupExpiredDocuments() {
        log.info("start cleanup expired Documents");
        LocalDateTime now = LocalDateTime.now();
        List<Document> expiredDocuments = documentRepo.findAllByPermanentDeleteAtBefore(now);
        for (Document expiredDoc : expiredDocuments) {
            documentService.hardDeleteItemById(expiredDoc.getId());
        }
        log.info("end cleanup expired Documents");
    }
}
