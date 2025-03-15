package vn.kltn.service;

import vn.kltn.common.RepoActionType;

public interface IRepoActivityService {
    void logActivity(Long repoId, RepoActionType action, String detail);
    void deleteActivitiesByRepoId(Long repoId);
}
