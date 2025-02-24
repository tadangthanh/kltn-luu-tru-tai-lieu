package vn.kltn.service;

import vn.kltn.common.RepoAction;

public interface IRepoActivityService {
    void logActivity(Long repoId, RepoAction action, String detail);
}
