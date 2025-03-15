package vn.kltn.service;

import vn.kltn.common.FileActionType;

public interface IFileActivityService {
    void logActivity(Long fileId, FileActionType action, String detail);

    void deleteActivitiesByRepoId(Long fileId);
}
