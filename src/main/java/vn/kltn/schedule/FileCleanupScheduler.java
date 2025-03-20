package vn.kltn.schedule;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import vn.kltn.service.IFileService;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "FILE_CLEANUP")
public class FileCleanupScheduler {
    private final IFileService fileService;

    // Chạy lúc 2 giờ sáng mỗi ngày
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void deleteExpiredFiles() {
        log.info("Xóa vĩnh viễn file hết hạn");
        fileService.deleteFileExpired();
    }
}
