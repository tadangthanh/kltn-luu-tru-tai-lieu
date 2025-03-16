package vn.kltn.aspect;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import vn.kltn.common.FileActionType;
import vn.kltn.dto.response.FileResponse;
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
    public void updateFileMetadataFilePointCut() {
    }

    @AfterReturning(value = "updateFileMetadataFilePointCut()", returning = "fileUpdated")
    public void logUpdateFileMetadata(JoinPoint joinPoint, FileResponse fileUpdated) {
        fileActivityService.logActivity(fileUpdated.getId(), FileActionType.UPDATE,
                String.format("Cập nhật metadata file #%s", fileUpdated.getId()));
    }

    @AfterReturning(value = "uploadFilePointCut()", returning = "fileRestore")
    public void logRestoreFile(JoinPoint joinPoint, FileResponse fileRestore) {
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
    public void logUploadFile(JoinPoint joinPoint, FileResponse fileUploaded) {
        fileActivityService.logActivity(fileUploaded.getId(), FileActionType.UPLOAD,
                String.format("Upload file %s vào repository %s", fileUploaded.getFileName(), fileUploaded.getId()));
    }
}
