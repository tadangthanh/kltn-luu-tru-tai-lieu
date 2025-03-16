package vn.kltn.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import vn.kltn.common.FileActionType;
import vn.kltn.dto.response.FileResponse;
import vn.kltn.dto.response.FileShareResponse;
import vn.kltn.service.IFileActivityService;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j(topic = "FILE_ACTIVITY")
public class FileActivityAspect {
    private final IFileActivityService fileActivityService;

    @Pointcut("execution(* vn.kltn.service.impl.FileServiceImpl.uploadFile(..))")
    public void uploadFilePointCut() {
    }

    @Pointcut("execution(* vn.kltn.service.impl.FileServiceImpl.deleteFile(..))")
    public void deleteFileFilePointCut() {
    }

    @Pointcut("execution(* vn.kltn.service.impl.FileServiceImpl.restoreFile(..))")
    public void restoreFileFilePointCut() {
    }

    @Pointcut("execution(* vn.kltn.service.impl.FileServiceImpl.updateFileMetadata(..))")
    public void updateFileMetadataPointCut() {
    }

    @Pointcut("execution(* vn.kltn.service.impl.FileServiceImpl.shareFile(..))")
    public void shareFilePointCut() {
    }

    @Pointcut("execution(* vn.kltn.service.impl.FileServiceImpl.deleteFileShareByFileId(..))")
    public void deleteFileShareByIdPointCut() {
    }

    @AfterReturning(value = "deleteFileShareByIdPointCut()")
    public void logDelFileShare(JoinPoint joinPoint) {
        log.info("{}.{}", joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName());
        Object[] args = joinPoint.getArgs();
        Long fileId = (Long) args[0];
        fileActivityService.logActivity(fileId, FileActionType.DELETE_SHARE,
                String.format("Xóa link chia sẻ file #%s ", fileId));
    }

    @AfterReturning(value = "shareFilePointCut()", returning = "fileShared")
    public void logShareFile(FileShareResponse fileShared) {
        log.info("Share file #%s: {}", fileShared.getFileId());
        fileActivityService.logActivity(fileShared.getFileId(), FileActionType.SHARE,
                String.format("Tạo link chia sẻ file #%s với thời gian hết hạn: %s", fileShared.getFileId(), fileShared.getExpireAt()));
    }

    @AfterReturning(value = "updateFileMetadataPointCut()", returning = "fileUpdated")
    public void logUpdateFileMetadata(FileResponse fileUpdated) {
        log.info("Update metadata file #%s: {}", fileUpdated.getId());
        fileActivityService.logActivity(fileUpdated.getId(), FileActionType.UPDATE,
                String.format("Cập nhật metadata file #%s", fileUpdated.getId()));
    }

    @AfterReturning(value = "restoreFileFilePointCut()", returning = "fileRestore")
    public void logRestoreFile(FileResponse fileRestore) {
        log.info("Restore file #%s: {}", fileRestore.getId());
        fileActivityService.logActivity(fileRestore.getId(), FileActionType.RESTORE,
                String.format("Khôi phục file đã xoá: #%s", fileRestore.getId()));
    }

    @AfterReturning(value = "deleteFileFilePointCut()")
    public void logDeleteFile(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        Long fileId = (Long) args[0];
        log.info("Delete file #%s: {}", fileId);
        fileActivityService.logActivity(fileId, FileActionType.DELETE,
                String.format("Xóa file #%s khỏi repository", fileId));
    }

    @AfterReturning(value = "uploadFilePointCut()", returning = "fileUploaded")
    public void logUploadFile(FileResponse fileUploaded) {
        log.info("Upload file #%s: {}", fileUploaded.getId());
        fileActivityService.logActivity(fileUploaded.getId(), FileActionType.UPLOAD,
                String.format("Upload file %s vào repository %s", fileUploaded.getFileName(), fileUploaded.getId()));
    }
}
