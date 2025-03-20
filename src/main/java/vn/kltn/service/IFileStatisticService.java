package vn.kltn.service;

import org.springframework.data.domain.Pageable;
import vn.kltn.dto.response.FileStatisticResponse;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.entity.File;

import java.util.List;

public interface IFileStatisticService {
    void increaseViewCount(Long fileId);

    void increaseDownloadCount(Long fileId);

    void increaseShareCount(Long fileId);

    void createFileStatistic(File file);

    void deleteByFileId(Long fileId);

    PageResponse<List<FileStatisticResponse>> getAllByFileId(Long fileId, Pageable pageable);
}
