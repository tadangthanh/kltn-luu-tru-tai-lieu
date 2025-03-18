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
import vn.kltn.repository.util.RepoUtil;
import vn.kltn.service.IAuthenticationService;
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
    private final RepoUtil repoUtil;


    @Before("@annotation(vn.kltn.validation.HasPermission)")
    public void checkPermissionMember(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        HasPermission annotation = method.getAnnotation(HasPermission.class);
        RepoPermission requiredPermission = annotation.value();

        Long repoId = repoUtil.getRepoIdByJoinPoint(joinPoint);
        // Lấy thông tin user
        User authUser = authService.getAuthUser();
        // Kiểm tra quyền
        if (!repoService.hasPermission(repoId, authUser.getId(), requiredPermission)) {
            throw new AccessDeniedException("Bạn không có quyền thực hiện hành động này");
        }
    }
}
