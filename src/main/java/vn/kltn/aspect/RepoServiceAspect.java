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
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.repository.RepositoryRepo;
import vn.kltn.service.IAuthenticationService;
import vn.kltn.service.IRepoMemberService;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j(topic = "vn.kltn.aspect.RepoServiceAspect")
public class RepoServiceAspect {
    private final IAuthenticationService authService;
    private final RepositoryRepo repositoryRepo;
    private final IRepoMemberService repoMemberService;

    @Before("@annotation(vn.kltn.validation.RequireOwner) && args(repoId,..)")
    public void checkOwnerRepoPermission(JoinPoint joinPoint, Long repoId) {
        log.info("Kiểm tra quyền chủ sở hũu repo, repoId: {}", repoId);
        Repo repo = getRepoByIdOrThrow(repoId);
        String authEmail = authService.getAuthUser().getEmail();
        if (!repo.getOwner().getEmail().equals(authEmail)) {
            log.error("{} Không phải chủ sở hữu repo, repoId: {}", authEmail, repoId);
            throw new AccessDeniedException("Bạn không có quyền thực hiện hành động này");
        }
    }

    @Before("@annotation(vn.kltn.validation.RequireRepoMember) && args(repoId,..)")
    public void checkRepoMembership(JoinPoint joinPoint, Long repoId) {
        log.info("Kiểm tra có phải thành viên repo, repoId: {}", repoId);
        User authUser = authService.getAuthUser();
        if (!repoMemberService.isExistMemberActiveByRepoIdAndUserId(repoId, authUser.getId())) {
            log.error("{} Không phải thành viên repo, repoId: {}", authUser.getEmail(), repoId);
            throw new AccessDeniedException("Bạn không có quyền thực hiện hành động này");
        }
    }

    private Repo getRepoByIdOrThrow(Long repoId) {
        return repositoryRepo.findById(repoId).orElseThrow(() -> new ResourceNotFoundException("Repository không tồn tại"));
    }
}
