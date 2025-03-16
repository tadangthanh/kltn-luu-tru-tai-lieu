package vn.kltn.aspect;

import lombok.RequiredArgsConstructor;
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
        Object[] args = joinPoint.getArgs();
        Long fileId = (Long) args[0];
        fileActivityService.logActivity(fileId, FileActionType.DELETE_SHARE,
                String.format("Xóa link chia sẻ file #%s ", fileId));
    }

    @AfterReturning(value = "shareFilePointCut()", returning = "fileShared")
    public void logShareFile(FileShareResponse fileShared) {
        fileActivityService.logActivity(fileShared.getId(), FileActionType.SHARE,
                String.format("Tạo link chia sẻ file #%s với thời gian hết hạn: %s", fileShared.getId(), fileShared.getExpireAt()));
    }

    @AfterReturning(value = "updateFileMetadataPointCut()", returning = "fileUpdated")
    public void logUpdateFileMetadata(FileResponse fileUpdated) {
        fileActivityService.logActivity(fileUpdated.getId(), FileActionType.UPDATE,
                String.format("Cập nhật metadata file #%s", fileUpdated.getId()));
    }

    @AfterReturning(value = "uploadFilePointCut()", returning = "fileRestore")
    public void logRestoreFile(FileResponse fileRestore) {
        fileActivityService.logActivity(fileRestore.getId(), FileActionType.RESTORE,
                String.format("Khôi phục file tên: %s, #%s", fileRestore.getFileName(), fileRestore.getId()));
    }

    @AfterReturning(value = "deleteFileFilePointCut()")
    public void logDeleteFile(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        Long fileId = (Long) args[0];
        fileActivityService.logActivity(fileId, FileActionType.DELETE,
                String.format("Xóa file #%s khỏi repository", fileId));
    }

    @AfterReturning(value = "uploadFilePointCut()", returning = "fileUploaded")
    public void logUploadFile(FileResponse fileUploaded) {
        fileActivityService.logActivity(fileUploaded.getId(), FileActionType.UPLOAD,
                String.format("Upload file %s vào repository %s", fileUploaded.getFileName(), fileUploaded.getId()));
    }
}
