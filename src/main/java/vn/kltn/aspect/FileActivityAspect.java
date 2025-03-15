package vn.kltn.aspect;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import vn.kltn.common.FileActionType;
import vn.kltn.dto.request.FileRequest;
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

    @AfterReturning(value = "uploadFilePointCut()", returning = "fileUploaded")
    public void logUploadFile(JoinPoint joinPoint, FileResponse fileUploaded) {
        fileActivityService.logActivity(fileUploaded.getId(), FileActionType.UPLOAD, String.format("Upload file %s vào repository %s", fileUploaded.getFileName(), fileUploaded.getId()));
    }
}
