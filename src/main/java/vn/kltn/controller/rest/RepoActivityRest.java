package vn.kltn.controller.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.dto.response.RepoActivityResponse;
import vn.kltn.dto.response.ResponseData;
import vn.kltn.service.IRepoActivityService;

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
}
