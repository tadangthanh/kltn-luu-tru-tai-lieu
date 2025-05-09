package vn.kltn.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import vn.kltn.entity.Folder;
import vn.kltn.entity.User;
import vn.kltn.service.IAuthenticationService;
import vn.kltn.service.IPermissionValidatorService;
import vn.kltn.service.impl.FolderCommonService;
import vn.kltn.validation.RequirePermission;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j(topic = "DOCUMENT_ASPECT")
public class DocumentAspect {
    private final IPermissionValidatorService permissionValidatorService;
    private final IAuthenticationService authenticationService;
    private final FolderCommonService folderCommonService;
    // Pointcut bắt method có annotation + tham số đầu tiên là Long parentId
    @Before("@annotation(requirePermission) && args(parentId,..)")
    public void checkPermission(JoinPoint joinPoint,
                                RequirePermission requirePermission,
                                Long parentId) {
        Folder parent = folderCommonService.getFolderByIdOrThrow(parentId);
        User currentUser = authenticationService.getCurrentUser();

        switch (requirePermission.value()) {
            case EDITOR:
                permissionValidatorService.validatePermissionEditor(parent, currentUser);
                break;
            case VIEWER:
                permissionValidatorService.validatePermissionViewer(parent, currentUser);
                break;
        }
    }
}
