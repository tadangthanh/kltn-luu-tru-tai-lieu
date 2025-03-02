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
import vn.kltn.service.IRepoService;
import vn.kltn.validation.ValidatePermissionMember;

import java.lang.reflect.Method;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j(topic = "vn.kltn.aspect.FileServiceAspect")
public class FileServiceAspect {
    private final IAuthenticationService authService;
    private final IRepoService repoService;


    @Before("@annotation(vn.kltn.validation.ValidatePermissionMember)")
    public void checkPermissionMember(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        Long repoId = (Long) args[0];
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        ValidatePermissionMember annotation = method.getAnnotation(ValidatePermissionMember.class);
        RepoPermission requiredPermission = annotation.value();
        User authUser = authService.getAuthUser();
        // Kiểm tra user có quyền hay không
        if (!repoService.hasPermission(repoId, authUser.getId(), requiredPermission)) {
            throw new AccessDeniedException("Bạn không có quyền thực hiện hành động này: " + requiredPermission);
        }
    }
}
