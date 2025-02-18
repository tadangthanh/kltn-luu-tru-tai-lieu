package vn.kltn.controller.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.kltn.dto.request.MemberRepoRequest;
import vn.kltn.dto.request.RepoRequestDto;
import vn.kltn.dto.response.RepoResponseDto;
import vn.kltn.dto.response.ResponseData;
import vn.kltn.service.IRepoService;

@RequiredArgsConstructor
@RestController
@Validated
@RequestMapping("/api/v1/repository")
public class RepositoryRest {
    private final IRepoService repositoryService;

    @PostMapping
    public ResponseData<RepoResponseDto> createRepo(@Validated @RequestBody RepoRequestDto repoRequestDto) {
        return new ResponseData<>(HttpStatus.CREATED.value(), "Tạo kho lưu trữ thành công",
                repositoryService.createRepository(repoRequestDto));
    }

    @DeleteMapping("/{repositoryId}")
    public ResponseData<Void> deleteRepository(@PathVariable Long repositoryId) {
        repositoryService.deleteRepository(repositoryId);
        return new ResponseData<>(HttpStatus.OK.value(), "Xóa kho lưu trữ thành công", null);
    }

    @PostMapping("/{repositoryId}/member")
    public ResponseData<RepoResponseDto> addMemberToRepository(@PathVariable Long repositoryId,
                                                               @Validated @RequestBody MemberRepoRequest memberRepoRequest) {
        return new ResponseData<>(HttpStatus.CREATED.value(), "Thêm thành viên vào kho lưu trữ thành công",
                repositoryService.addMemberToRepository(repositoryId, memberRepoRequest.getUserId(), memberRepoRequest.getPermissions()));
    }
}
