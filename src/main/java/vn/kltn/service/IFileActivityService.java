package vn.kltn.service;

import org.springframework.data.domain.Pageable;
import vn.kltn.common.FileActionType;
import vn.kltn.dto.response.FileActivityResponse;
import vn.kltn.dto.response.PageResponse;

import java.time.LocalDate;
import java.util.List;

public interface IFileActivityService {
    void logActivity(Long fileId, FileActionType action, String detail);

    void deleteActivitiesByFileId(Long fileId);

    PageResponse<List<FileActivityResponse>> advanceSearchBySpecification(Long fileId, Pageable pageable, String[] activities);

    PageResponse<List<FileActivityResponse>> searchByStartDateAndEndDate(Long fileId, Pageable pageable, LocalDate startDate, LocalDate endDate);
}
