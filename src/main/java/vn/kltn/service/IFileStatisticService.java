package vn.kltn.service;

import vn.kltn.entity.File;

public interface IFileStatisticService {
    void increaseViewCount(Long fileId);

    void increaseDownloadCount(Long fileId);

    void increaseShareCount(Long fileId);

    void createFileStatistic(File file);

    void deleteFileStatisticByFileId(Long fileId);
}
