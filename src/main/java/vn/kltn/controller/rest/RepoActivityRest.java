package vn.kltn.controller.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.dto.response.RepoActivityResponse;
import vn.kltn.dto.response.ResponseData;
import vn.kltn.service.IRepoActivityService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/repository-activity")
public class RepoActivityRest {
    private final IRepoActivityService repoActivityService;

    @GetMapping("/{repoId}")
    public ResponseData<PageResponse<List<RepoActivityResponse>>> getPage(@PathVariable Long repoId, Pageable pageable) {
        return new ResponseData<>(HttpStatus.OK.value(), "Get activities by repoId successfully",
                repoActivityService.getActivitiesByRepoId(repoId, pageable));
    }

    @GetMapping("/{repoId}/search-by-date-range")
    public ResponseData<PageResponse<List<RepoActivityResponse>>> searchByStartDateAndEndDate(
            Pageable pageable, @PathVariable Long repoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return new ResponseData<>(200, "Search activity repository by date range successfully",
                repoActivityService.searchByStartDateAndEndDate(repoId, pageable, startDate, endDate));
    }
}
