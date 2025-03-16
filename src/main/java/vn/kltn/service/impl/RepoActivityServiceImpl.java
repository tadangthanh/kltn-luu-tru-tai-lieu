package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.kltn.common.RepoActionType;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.dto.response.RepoActivityResponse;
import vn.kltn.entity.Repo;
import vn.kltn.entity.RepoActivity;
import vn.kltn.entity.User;
import vn.kltn.map.RepoActivityMapper;
import vn.kltn.repository.RepositoryActivityRepo;
import vn.kltn.repository.util.PaginationUtils;
import vn.kltn.service.IAuthenticationService;
import vn.kltn.service.IRepoActivityService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j(topic = "REPOSITORY_ACTIVITY_SERVICE")
public class RepoActivityServiceImpl implements IRepoActivityService {
    private final RepositoryActivityRepo activityRepo;
    private final RepoCommonService repoCommonService;
    private final IAuthenticationService authService;
    private final RepoActivityMapper repoActivityMapper;

    @Override
    public void logActivity(Long repoId, RepoActionType action, String detail) {
        log.info("Log activity for repoId: {}, action: {}, detail: {}", repoId, action, detail);
        Repo repo = repoCommonService.getRepositoryById(repoId);
        saveActivity(repo, action, detail);
    }

    @Override
    public void deleteActivitiesByRepoId(Long repoId) {
        log.info("Delete activities by repoId: {}", repoId);
        activityRepo.deleteByRepoId(repoId);
    }

    @Override
    public PageResponse<List<RepoActivityResponse>> searchByStartDateAndEndDate(Long repoId, Pageable pageable, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startOfDay = startDate.atStartOfDay(); // 2025-03-05 00:00:00
        LocalDateTime endOfDay = endDate.atTime(23, 59, 59); // 2025-03-10 23:59:59
        Page<RepoActivity> repoActivityPage = activityRepo.findActiveRepositoriesByRepoIdAndCreatedAtRange(repoId, startOfDay, endOfDay, pageable);
        return PaginationUtils.convertToPageResponse(repoActivityPage, pageable, repoActivityMapper::toResponse);
    }

    @Override
    public PageResponse<List<RepoActivityResponse>> getActivitiesByRepoId(Long repoId, Pageable pageable) {
        Page<RepoActivity> repoActivityPage = activityRepo.findByRepoId(repoId, pageable);
        return PaginationUtils.convertToPageResponse(repoActivityPage, pageable, repoActivityMapper::toResponse);
    }

    private void saveActivity(Repo repo, RepoActionType action, String detail) {
        RepoActivity activity = new RepoActivity();
        User authUser = authService.getAuthUser();
        activity.setRepo(repo);
        activity.setUser(authUser);
        activity.setAction(action);
        activity.setDetails(detail);
        activityRepo.save(activity);
    }

}
