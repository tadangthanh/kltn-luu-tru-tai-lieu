package vn.kltn.controller.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.kltn.dto.request.RepoMemberRequest;
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
                                                               @Validated @RequestBody RepoMemberRequest repoMemberRequest) {
        return new ResponseData<>(HttpStatus.CREATED.value(), "Thêm thành viên vào kho lưu trữ thành công",
                repositoryService.addMemberToRepository(repositoryId, repoMemberRequest.getUserId(), repoMemberRequest.getPermissions()));
    }

    @PostMapping("/{repositoryId}/member/{userId}")
    public ResponseData<RepoResponseDto> updatePermissionForMember(@PathVariable Long repositoryId,
                                                                   @PathVariable Long userId,
                                                                   @Validated @RequestBody RepoMemberRequest repoMemberRequest) {
        return new ResponseData<>(HttpStatus.OK.value(), "Cập nhật quyền cho thành viên thành công",
                repositoryService.updatePermissionMember(repositoryId, userId, repoMemberRequest.getPermissions()));
    }

    @DeleteMapping("/{repositoryId}/member/{memberId}")
    public ResponseData<Void> removeMemberFromRepository(@PathVariable Long repositoryId, @PathVariable Long memberId) {
        repositoryService.removeMemberFromRepository(repositoryId, memberId);
        return new ResponseData<>(HttpStatus.OK.value(), "Xóa thành viên khỏi kho lưu trữ thành công", null);
    }
}
