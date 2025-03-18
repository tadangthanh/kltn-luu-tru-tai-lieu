package vn.kltn.repository.util;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import vn.kltn.exception.InvalidDataException;
import vn.kltn.service.IFileService;

@Component
@RequiredArgsConstructor
public class RepoUtil {

    private final IFileService fileService;

    public Long getRepoIdByJoinPoint(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = signature.getParameterNames();

        Long repoId = null;
        Long fileId = null;

        for (int i = 0; i < parameterNames.length; i++) {
            if (args[i] instanceof Long id) {
                if ("repoId".equals(parameterNames[i])) {
                    repoId = id;
                } else if ("fileId".equals(parameterNames[i])) {
                    fileId = id;
                }
            }
        }

        return resolveRepoId(repoId, fileId);
    }

    private Long resolveRepoId(Long repoId, Long fileId) {
        if (repoId != null) return repoId;
        if (fileId != null) return fileService.getRepoIdByFileId(fileId);
        throw new InvalidDataException("Không xác định được repository.");
    }
}
