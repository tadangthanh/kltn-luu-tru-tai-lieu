package vn.kltn.controller.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import vn.kltn.dto.response.FileActivityResponse;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.dto.response.ResponseData;
import vn.kltn.service.IFileActivityService;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/file-activity")
public class FileActivityRest {
    private final IFileActivityService fileActivityService;

    @GetMapping("/{fileId}/search-by-date-range")
    public ResponseData<PageResponse<List<FileActivityResponse>>> searchByStartDateAndEndDate(
            Pageable pageable, @PathVariable Long fileId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return new ResponseData<>(200, "Search activity repository by date range successfully",
                fileActivityService.searchByStartDateAndEndDate(fileId, pageable, startDate, endDate));
    }

    @GetMapping("/{fileId}/search")
    public ResponseData<PageResponse<List<FileActivityResponse>>> searchActivity(Pageable pageable, @PathVariable Long fileId,
                                                                                 @RequestParam(required = false, value = "activities") String[] activities) {
        return new ResponseData<>(200, "Search activity successfully", fileActivityService.advanceSearchBySpecification(fileId, pageable, activities));
    }
}
