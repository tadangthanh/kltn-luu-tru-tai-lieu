package vn.kltn.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j(topic = "vn.kltn.aspect.FileStatisticAspect")
public class FileStatisticAspect {
//    private final IFileStatisticService fileStatisticService;
//
//    @Pointcut("execution(* vn.kltn.service.impl.FileServiceImpl.shareFile(..))")
//    public void shareFile() {
//    }
//
//    @Pointcut("execution(* vn.kltn.service.impl.FileServiceImpl.downloadFile(..))")
//    public void downloadFile() {
//    }
//
//    @Pointcut("execution(* vn.kltn.service.impl.FileServiceImpl.viewFile(..))")
//    public void viewFile() {
//    }
//
//    @AfterReturning(value = "viewFile()", returning = "fileDataResponse")
//    public void increaseDownloadFile(FileDataResponse fileDataResponse) {
//        Long fileId = fileDataResponse.getFileId();
//        log.info("Increase view count for file #{}", fileId);
//        fileStatisticService.increaseViewCount(fileId);
//    }
//
//    @AfterReturning(value = "downloadFile() && args(fileId)")
//    public void increaseDownloadFile(Long fileId) {
//        log.info("Increase download count for file #{}", fileId);
//        fileStatisticService.increaseDownloadCount(fileId);
//    }
//
//    @AfterReturning(value = "shareFile()", returning = "response")
//    public void increaseShareFile(FileShareResponse response) {
//        // Đảm bảo chỉ tăng số liệu nếu việc share thành công
//        if (response != null) {
//            log.info("Increase share count for file {}", response.getFileId());
//            fileStatisticService.increaseShareCount(response.getFileId());
//        }
//    }

}
