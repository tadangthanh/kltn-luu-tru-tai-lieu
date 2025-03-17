package vn.kltn.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import vn.kltn.entity.Repo;
import vn.kltn.entity.User;
import vn.kltn.exception.InvalidDataException;
import vn.kltn.service.IAuthenticationService;
import vn.kltn.service.IFileService;
import vn.kltn.service.IRepoMemberService;
import vn.kltn.service.IRepoService;
import vn.kltn.validation.HasPermission;

import java.lang.reflect.Method;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j(topic = "vn.kltn.aspect.RepoServiceAspect")
public class RepoServiceAspect {
    private final IAuthenticationService authService;
    private final IRepoMemberService repoMemberService;
    private final IRepoService repoService;
    private final IFileService fileService;

    @Before("@annotation(vn.kltn.validation.RequireOwnerRepo) && args(repoId,..)")
    public void checkOwnerRepoPermission(Long repoId) {
        log.info("Kiểm tra quyền chủ sở hũu repo, repoId: {}", repoId);
        Repo repo = repoService.getRepositoryById(repoId);
        String authEmail = authService.getAuthUser().getEmail();
        if (!repo.getOwner().getEmail().equals(authEmail)) {
            log.error("{} Không phải chủ sở hữu repo, repoId: {}", authEmail, repoId);
            throw new AccessDeniedException("Bạn không có quyền thực hiện hành động này");
        }
    }

    //    @Before("@annotation(vn.kltn.validation.RequireRepoMemberActive) && args(repoId,..)")
//    public void checkRepoMembership(Long repoId) {
//        log.info("Kiểm tra có phải thành viên repo, repoId: {}", repoId);
//        User authUser = authService.getAuthUser();
//        if (!repoMemberService.isExistMemberActiveByRepoIdAndUserId(repoId, authUser.getId())) {
//            log.error("{} Không phải thành viên repo, repoId: {}", authUser.getEmail(), repoId);
//            throw new AccessDeniedException("Bạn không có quyền thực hiện hành động này");
//        }
//    }
    @Before("@annotation(vn.kltn.validation.RequireRepoMemberActive)")
    public void checkRepoMembership(JoinPoint joinPoint) {
        log.info("Kiểm tra có phải thành viên repo");
        Object[] args = joinPoint.getArgs();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

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
            throw new InvalidDataException("Có lỗi xảy ra khi xác định quyền hạn của bạn");
        }

        User authUser = authService.getAuthUser();
        if (!repoMemberService.isExistMemberActiveByRepoIdAndUserId(repoId, authUser.getId())) {
            log.error("{} Không phải thành viên repo, repoId: {}", authUser.getEmail(), repoId);
            throw new AccessDeniedException("Bạn không có quyền thực hiện hành động này");
        }
    }
}
