package vn.kltn.controller.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.kltn.dto.request.RepoMemberRequest;
import vn.kltn.dto.request.RepoRequestDto;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.dto.response.RepoMemberInfoResponse;
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
    public ResponseData<PageResponse<List<RepoResponseDto>>> getRepoList( Pageable pageable) {
        return new ResponseData<>(HttpStatus.OK.value(), "Lấy danh s kho lưu trữ thành công",
                repositoryService.getPageResponseByUserAuth(pageable));
    }
    @DeleteMapping("/{repositoryId}")
    public ResponseData<Void> deleteRepository(@PathVariable Long repositoryId) {
        repositoryService.deleteRepository(repositoryId);
        return new ResponseData<>(HttpStatus.NO_CONTENT.value(), "Xóa kho lưu trữ thành công", null);
    }

    @PostMapping("/{repositoryId}/member")
    public ResponseData<RepoResponseDto> addMemberToRepository(@PathVariable Long repositoryId,
                                                               @Validated(Create.class) @RequestBody RepoMemberRequest repoMemberRequest) {
        return new ResponseData<>(HttpStatus.CREATED.value(), "gửi lơi mời thành công",
                repositoryService.addMemberToRepository(repositoryId, repoMemberRequest.getUserId(), repoMemberRequest.getPermissions()));
    }

    @PostMapping("/{repositoryId}/member/{userId}")
    public ResponseData<RepoMemberInfoResponse> updatePermissionForMember(@PathVariable Long repositoryId,
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

    @GetMapping("/{repositoryId}/members")
    public ResponseData<PageResponse<List<RepoMemberInfoResponse>>> getListMember(@PathVariable Long repositoryId, Pageable pageable) {
        return new ResponseData<>(HttpStatus.OK.value(), "Lấy danh sách thành viên thành công",
                repositoryService.getListMemberByRepoId(repositoryId, pageable));
    }

    @PostMapping("/{repositoryId}/member/{userId}/enable")
    public ResponseData<RepoMemberInfoResponse> enableMember(@PathVariable Long repositoryId, @PathVariable Long userId) {
        return new ResponseData<>(HttpStatus.OK.value(), "Kích hoạt thành viên thành công",
                repositoryService.enableMemberByRepoIdAndUserId(repositoryId, userId));
    }

    @PostMapping("/{repositoryId}/member/{userId}/disable")
    public ResponseData<RepoMemberInfoResponse> disableMember(@PathVariable Long repositoryId, @PathVariable Long userId) {
        return new ResponseData<>(HttpStatus.OK.value(), "Vô hiệu hóa thành viên thành công",
                repositoryService.disableMemberByRepoIdAndUserId(repositoryId, userId));
    }

    @PostMapping("/{repositoryId}/member/leave")
    public ResponseData<RepoMemberInfoResponse> leaveRepo(@PathVariable Long repositoryId) {
        return new ResponseData<>(HttpStatus.OK.value(), "Rời khỏi kho lưu trữ thành công",
                repositoryService.leaveRepo(repositoryId));
    }
}
