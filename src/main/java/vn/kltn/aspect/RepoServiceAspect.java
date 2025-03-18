package vn.kltn.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import vn.kltn.entity.Repo;
import vn.kltn.entity.User;
import vn.kltn.repository.util.RepoUtil;
import vn.kltn.service.IAuthenticationService;
import vn.kltn.service.IRepoMemberService;
import vn.kltn.service.IRepoService;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j(topic = "vn.kltn.aspect.RepoServiceAspect")
public class RepoServiceAspect {
    private final IAuthenticationService authService;
    private final IRepoMemberService repoMemberService;
    private final IRepoService repoService;
    private final RepoUtil repoUtil;

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

    @Before("@annotation(vn.kltn.validation.RequireRepoMemberActive)")
    public void checkRepoMembership(JoinPoint joinPoint) {
        log.info("Kiểm tra có phải thành viên repo");
        Long repoId = repoUtil.getRepoIdByJoinPoint(joinPoint);
        User authUser = authService.getAuthUser();
        if (!repoMemberService.isExistMemberActiveByRepoIdAndUserId(repoId, authUser.getId())) {
            log.error("{} Không phải thành viên repo, repoId: {}", authUser.getEmail(), repoId);
            throw new AccessDeniedException("Bạn không có quyền thực hiện hành động này");
        }
    }


}
