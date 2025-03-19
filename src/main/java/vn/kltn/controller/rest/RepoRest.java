package vn.kltn.controller.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.kltn.dto.request.MemberRequest;
import vn.kltn.dto.request.RepoRequestDto;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.dto.response.RepoResponseDto;
import vn.kltn.dto.response.ResponseData;
import vn.kltn.service.IRepoService;
import vn.kltn.validation.Create;
import vn.kltn.validation.Update;

import java.util.List;

@RequiredArgsConstructor
@RestController
@Validated
@RequestMapping("/api/v1/repository")
public class RepoRest {
    private final IRepoService repositoryService;

    @PostMapping
    public ResponseData<RepoResponseDto> createRepo(@Validated(Create.class) @RequestBody RepoRequestDto repoRequestDto) {
        return new ResponseData<>(HttpStatus.CREATED.value(), "Tạo kho lưu trữ thành công",
                repositoryService.createRepository(repoRequestDto));
    }

    @GetMapping
    public ResponseData<PageResponse<List<RepoResponseDto>>> getRepoList(Pageable pageable) {
        return new ResponseData<>(HttpStatus.OK.value(), "Lấy danh sách kho lưu trữ thành công",
                repositoryService.getPageResponseByUserAuth(pageable));
    }

    @DeleteMapping("/{repositoryId}")
    public ResponseData<Void> deleteRepository(@PathVariable Long repositoryId) {
        repositoryService.deleteRepository(repositoryId);
        return new ResponseData<>(HttpStatus.NO_CONTENT.value(), "Xóa kho lưu trữ thành công", null);
    }

    @PostMapping("/{repositoryId}/member")
    public ResponseData<RepoResponseDto> addMemberToRepository(@PathVariable Long repositoryId,
                                                               @Validated(Create.class) @RequestBody MemberRequest memberRequest) {
        return new ResponseData<>(HttpStatus.CREATED.value(), "gửi lơi mời thành công",
                repositoryService.addMemberToRepository(repositoryId, memberRequest.getUserId(), memberRequest.getRoleId()));
    }

    @PatchMapping("/{repositoryId}")
    public ResponseData<RepoResponseDto> updateRepository(@PathVariable Long repositoryId,
                                                          @Validated(Update.class) @RequestBody RepoRequestDto repoRequestDto) {
        return new ResponseData<>(HttpStatus.OK.value(), "Cập nhật kho lưu trữ thành công",
                repositoryService.update(repositoryId, repoRequestDto));
    }


}
