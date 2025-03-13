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
import vn.kltn.validation.Create;
import vn.kltn.validation.Update;

@RequiredArgsConstructor
@RestController
@Validated
@RequestMapping("/api/v1/repository")
public class RepositoryRest {
    private final IRepoService repositoryService;

    @PostMapping
    public ResponseData<RepoResponseDto> createRepo(@Validated(Create.class) @RequestBody RepoRequestDto repoRequestDto) {
        return new ResponseData<>(HttpStatus.CREATED.value(), "Tạo kho lưu trữ thành công",
                repositoryService.createRepository(repoRequestDto));
    }

    @DeleteMapping("/{repositoryId}")
    public ResponseData<Void> deleteRepository(@PathVariable Long repositoryId) {
        repositoryService.deleteRepository(repositoryId);
        return new ResponseData<>(HttpStatus.NO_CONTENT.value(), "Xóa kho lưu trữ thành công", null);
    }

    @PostMapping("/{repositoryId}/member")
    public ResponseData<RepoResponseDto> addMemberToRepository(@PathVariable Long repositoryId,
                                                               @Validated(Create.class) @RequestBody RepoMemberRequest repoMemberRequest) {
        return new ResponseData<>(HttpStatus.CREATED.value(), "Thêm thành viên vào kho lưu trữ thành công",
                repositoryService.addMemberToRepository(repositoryId, repoMemberRequest.getUserId(), repoMemberRequest.getPermissions()));
    }
    @PostMapping("/{repositoryId}/member/{userId}")
    public ResponseData<RepoResponseDto> updatePermissionForMember(@PathVariable Long repositoryId,
                                                                   @PathVariable Long userId,
                                                                   @Validated(Update.class) @RequestBody RepoMemberRequest repoMemberRequest) {
        return new ResponseData<>(HttpStatus.OK.value(), "Cập nhật quyền cho thành viên thành công",
                repositoryService.updatePermissionMemberByRepoIdAndUserId(repositoryId, userId, repoMemberRequest.getPermissions()));
    }

    @DeleteMapping("/{repositoryId}/member/{userId}")
    public ResponseData<Void> removeMemberFromRepository(@PathVariable Long repositoryId, @PathVariable Long userId) {
        repositoryService.removeMemberByRepoIdAndUserId(repositoryId, userId);
        return new ResponseData<>(HttpStatus.NO_CONTENT.value(), "Xóa thành viên khỏi kho lưu trữ thành công", null);
    }

    @PatchMapping("/{repositoryId}")
    public ResponseData<RepoResponseDto> updateRepository(@PathVariable Long repositoryId,
                                                          @Validated(Update.class) @RequestBody RepoRequestDto repoRequestDto) {
        return new ResponseData<>(HttpStatus.OK.value(), "Cập nhật kho lưu trữ thành công",
                repositoryService.update(repositoryId, repoRequestDto));
    }
}
