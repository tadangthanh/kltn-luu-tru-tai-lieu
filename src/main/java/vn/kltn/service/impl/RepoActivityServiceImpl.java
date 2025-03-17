package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import vn.kltn.common.RepoActionType;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.dto.response.RepoActivityResponse;
import vn.kltn.entity.Repo;
import vn.kltn.entity.RepoActivity;
import vn.kltn.entity.User;
import vn.kltn.map.RepoActivityMapper;
import vn.kltn.repository.RepoActivityRepo;
import vn.kltn.repository.specification.EntitySpecificationsBuilder;
import vn.kltn.repository.util.PaginationUtils;
import vn.kltn.service.IAuthenticationService;
import vn.kltn.service.IRepoActivityService;
import vn.kltn.service.IUserService;
import vn.kltn.validation.RequireRepoMemberActive;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j(topic = "REPOSITORY_ACTIVITY_SERVICE")
public class RepoActivityServiceImpl implements IRepoActivityService {
    private final RepoActivityRepo activityRepo;
    private final RepoCommonService repoCommonService;
    private final IAuthenticationService authService;
    private final RepoActivityMapper repoActivityMapper;
    private final IUserService  userService;

    @Override
    @RequireRepoMemberActive
    public void logActivity(Long repoId, RepoActionType action, String detail) {
        log.info("Log activity for repoId: {}, action: {}, detail: {}", repoId, action, detail);
        Repo repo = repoCommonService.getRepositoryById(repoId);
        saveActivityByAuthUser(repo, action, detail);
    }

    @Override
    public void logActivity(Long repoId, String actionByEmail, RepoActionType action, String detail) {
        log.info("Log activity for repoId: {}, action: {}, action by email: {}, detail: {}", repoId, action, actionByEmail,detail);
        Repo repo = repoCommonService.getRepositoryById(repoId);
        saveActivityByActionByEmail(repo,actionByEmail, action, detail);
    }

    @Override
    public void deleteActivitiesByRepoId(Long repoId) {
        log.info("Delete activities by repoId: {}", repoId);
        activityRepo.deleteByRepoId(repoId);

    }

    @Override
    @RequireRepoMemberActive
    public PageResponse<List<RepoActivityResponse>> advanceSearchBySpecification(Long repoId, Pageable pageable, String[] activities) {
        log.info("request search activity with specification");
        if (activities != null && activities.length > 0) {
            EntitySpecificationsBuilder<RepoActivity> builder = new EntitySpecificationsBuilder<>();
//            Pattern pattern = Pattern.compile("(\\w+?)([<:>~!])(.*)(\\p{Punct}?)(\\p{Punct}?)");
            Pattern pattern = Pattern.compile("([a-zA-Z0-9_.]+?)([<:>~!])(.*)(\\p{Punct}?)(\\p{Punct}?)");
            //patten chia ra thành 5 nhóm
            // nhóm 1: từ cần tìm kiếm (có thể là tên cột hoặc tên bảng) , ví dụ: name, age, subTopic.id=> subTopic là tên bảng, id là tên cột
            // nhóm 2: toán tử tìm kiếm
            // nhóm 3: giá trị cần tìm kiếm
            // nhóm 4: dấu câu cuối cùng
            // nhóm 5: dấu câu cuối cùng
            for (String s : activities) {
                Matcher matcher = pattern.matcher(s);
                if (matcher.find()) {
                    builder.with(matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4), matcher.group(5));
                }
            }
            Specification<RepoActivity> spec = builder.build();
            // nó trả trả về 1 spec mới
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("repo").get("id"), repoId));
            Page<RepoActivity> filePage = activityRepo.findAll(spec, pageable);
            return PaginationUtils.convertToPageResponse(filePage, pageable, repoActivityMapper::toResponse);
        }
        return PaginationUtils.convertToPageResponse(activityRepo.findAll(pageable), pageable, repoActivityMapper::toResponse);
    }

    @Override
    @RequireRepoMemberActive
    public PageResponse<List<RepoActivityResponse>> searchByStartDateAndEndDate(Long repoId, Pageable pageable, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startOfDay = startDate.atStartOfDay(); // 2025-03-05 00:00:00
        LocalDateTime endOfDay = endDate.atTime(23, 59, 59); // 2025-03-10 23:59:59
        Page<RepoActivity> repoActivityPage = activityRepo.findActivityRepositoriesByRepoIdAndCreatedAtRange(repoId, startOfDay, endOfDay, pageable);
        return PaginationUtils.convertToPageResponse(repoActivityPage, pageable, repoActivityMapper::toResponse);
    }

    @Override
    @RequireRepoMemberActive
    public PageResponse<List<RepoActivityResponse>> getActivitiesByRepoId(Long repoId, Pageable pageable) {
        Page<RepoActivity> repoActivityPage = activityRepo.findByRepoId(repoId, pageable);
        return PaginationUtils.convertToPageResponse(repoActivityPage, pageable, repoActivityMapper::toResponse);
    }

    private void saveActivityByAuthUser(Repo repo, RepoActionType action, String detail) {
        RepoActivity activity = new RepoActivity();
        User authUser = authService.getAuthUser();
        activity.setRepo(repo);
        activity.setUser(authUser);
        activity.setAction(action);
        activity.setDetails(detail);
        activityRepo.save(activity);
    }
    private void saveActivityByActionByEmail(Repo repo,String actionByEmail ,RepoActionType action, String detail) {
        RepoActivity activity = new RepoActivity();
        User authUser = userService.getUserByEmail(actionByEmail);
        activity.setRepo(repo);
        activity.setUser(authUser);
        activity.setAction(action);
        activity.setDetails(detail);
        activityRepo.save(activity);
    }

}
