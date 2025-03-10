package vn.kltn.controller.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.dto.response.RepoMemberInfoResponse;
import vn.kltn.dto.response.ResponseData;
import vn.kltn.service.IRepoMemberService;

import java.util.List;

@RequiredArgsConstructor
@RestController
@Validated
@RequestMapping("/api/v1/repo-member")
public class RepoMemberRest {
    private final IRepoMemberService repoMemberService;

    @GetMapping
    public ResponseData<PageResponse<List<RepoMemberInfoResponse>>> getListMember(@RequestParam Long repoId, Pageable pageable) {
        return new ResponseData<>(HttpStatus.OK.value(), "Lấy danh sách thành viên thành công",
                repoMemberService.getListMemberByRepoId(repoId, pageable));
    }

}
