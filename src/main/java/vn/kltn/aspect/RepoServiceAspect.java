package vn.kltn.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import vn.kltn.entity.Repo;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.repository.RepositoryRepo;
import vn.kltn.service.IAuthenticationService;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j(topic = "vn.kltn.aspect.RepoServiceAspect")
public class RepoServiceAspect {
    private final IAuthenticationService authService;
    private final RepositoryRepo repositoryRepo;

    @Before("@annotation(vn.kltn.validation.RequireOwner) && args(repoId,..)")
    public void checkOwnerPermission(JoinPoint joinPoint, Long repoId) {
        log.info("Kiểm tra quyền chủ sở hũu repo, repoId: {}", repoId);
        Repo repo = repositoryRepo.findById(repoId).orElseThrow(() -> new ResourceNotFoundException("Repository không tồn tại"));
        String authEmail = authService.getAuthUser().getEmail();
        if (!repo.getOwner().getEmail().equals(authEmail)) {
            log.error("{} Không phải chủ sở hữu repo, repoId: {}", authEmail, repoId);
            throw new AccessDeniedException("Bạn không có quyền thực hiện hành động này");
        }
    }
}
