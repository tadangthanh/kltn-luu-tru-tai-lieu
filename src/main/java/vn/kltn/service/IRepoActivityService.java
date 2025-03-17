package vn.kltn.service;

import org.springframework.data.domain.Pageable;
import vn.kltn.common.RepoActionType;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.dto.response.RepoActivityResponse;

import java.time.LocalDate;
import java.util.List;

public interface IRepoActivityService {
    void logActivity(Long repoId, RepoActionType action, String detail);

    void logActivity(Long repoId,String actionByEmail, RepoActionType action, String detail);

    void deleteActivitiesByRepoId(Long repoId);

    PageResponse<List<RepoActivityResponse>> advanceSearchBySpecification(Long repoId, Pageable pageable, String[] activities);

    PageResponse<List<RepoActivityResponse>> searchByStartDateAndEndDate(Long repoId, Pageable pageable, LocalDate startDate, LocalDate endDate);

    PageResponse<List<RepoActivityResponse>> getActivitiesByRepoId(Long repoId, Pageable pageable);
}
