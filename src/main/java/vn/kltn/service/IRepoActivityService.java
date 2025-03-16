package vn.kltn.service;

import org.springframework.data.domain.Pageable;
import vn.kltn.common.RepoActionType;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.dto.response.RepoActivityResponse;

import java.util.List;

public interface IRepoActivityService {
    void logActivity(Long repoId, RepoActionType action, String detail);

    void deleteActivitiesByRepoId(Long repoId);

    PageResponse<List<RepoActivityResponse>> getActivitiesByRepoId(Long repoId, Pageable pageable);
}
