package vn.kltn.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import vn.kltn.common.RepoPermission;
import vn.kltn.entity.User;
import vn.kltn.service.IAuthenticationService;
import vn.kltn.service.IFileService;
import vn.kltn.service.IRepoService;
import vn.kltn.validation.HasPermission;

import java.lang.reflect.Method;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j(topic = "vn.kltn.aspect.FileServiceAspect")
public class FileServiceAspect {
    private final IAuthenticationService authService;
    private final IRepoService repoService;
    private final IFileService fileService;


    @Before("@annotation(vn.kltn.validation.HasPermission)")
    public void checkPermissionMember(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        HasPermission annotation = method.getAnnotation(HasPermission.class);
        RepoPermission requiredPermission = annotation.value();

        // Lấy danh sách tham số của method
        String[] parameterNames = signature.getParameterNames();

        Long repoId = null;
        Long fileId = null;

        // Duyệt qua danh sách tham số để xác định đâu là `repoId` hoặc `fileId`
        for (int i = 0; i < parameterNames.length; i++) {
            if ("repoId".equals(parameterNames[i]) && args[i] instanceof Long) {
                repoId = (Long) args[i];  // Nếu method có tham số tên "repoId", thì lấy luôn
            } else if ("fileId".equals(parameterNames[i]) && args[i] instanceof Long) {
                fileId = (Long) args[i];  // Nếu có tham số "fileId", thì lấy để truy vấn repoId
            }
        }

        // Nếu không có repoId nhưng có fileId => Truy vấn repoId từ fileId
        if (repoId == null && fileId != null) {
            repoId = fileService.getRepoIdByFileId(fileId);
        }

        // Nếu vẫn không có repoId, báo lỗi
        if (repoId == null) {
            throw new IllegalArgumentException("Không thể xác định repoId để kiểm tra quyền.");
        }

        // Lấy thông tin user
        User authUser = authService.getAuthUser();

        // Kiểm tra quyền
        if (!repoService.hasPermission(repoId, authUser.getId(), requiredPermission)) {
            throw new AccessDeniedException("Bạn không có quyền thực hiện hành động này");
        }
    }
}
