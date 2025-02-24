package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.kltn.common.RepoAction;
import vn.kltn.entity.Repo;
import vn.kltn.entity.RepoActivity;
import vn.kltn.entity.User;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.repository.RepositoryActivityRepo;
import vn.kltn.repository.RepositoryRepo;
import vn.kltn.service.IAuthenticationService;
import vn.kltn.service.IRepoActivityService;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j(topic = "REPOSITORY_ACTIVITY_SERVICE")
public class RepoActivityServiceImpl implements IRepoActivityService {
    private final RepositoryActivityRepo activityRepo;
    private final RepositoryRepo repositoryRepo;
    private final IAuthenticationService authService;

    @Override
    public void logActivity(Long repoId, RepoAction action, String detail) {
        log.info("Log activity for repoId: {}, action: {}, detail: {}", repoId, action, detail);
        Repo repo = getRepoByIdOrThrow(repoId);
        saveActivity(repo, action, detail);
    }

    @Override
    public void deleteActivitiesByRepoId(Long repoId) {
        log.info("Delete activities by repoId: {}", repoId);
        activityRepo.deleteByRepoId(repoId);
    }

    private void saveActivity(Repo repo, RepoAction action, String detail) {
        RepoActivity activity = new RepoActivity();
        User authUser = authService.getAuthUser();
        activity.setRepo(repo);
        activity.setUser(authUser);
        activity.setAction(action);
        activity.setDetails(detail);
        activityRepo.save(activity);
    }

    private Repo getRepoByIdOrThrow(Long repoId) {
        return repositoryRepo.findById(repoId).orElseThrow(() -> {
            log.error("Repo not found with id: {}", repoId);
            return new ResourceNotFoundException("Không tìm thấy repo id: " + repoId);
        });
    }
}
