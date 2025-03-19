package vn.kltn.controller.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.kltn.dto.request.MemberRequest;
import vn.kltn.dto.response.MemberResponse;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.dto.response.ResponseData;
import vn.kltn.service.IMemberService;
import vn.kltn.validation.Update;

import java.util.List;

@RequiredArgsConstructor
@RestController
@Validated
@RequestMapping("/api/v1/member")
public class MemberRest {
    private final IMemberService memberService;

    @PostMapping("/{repositoryId}/user/{userId}")
    public ResponseData<MemberResponse> updatePermissionForMember(@PathVariable Long repositoryId,
                                                                  @PathVariable Long userId,
                                                                  @Validated(Update.class) @RequestBody MemberRequest memberRequest) {
        return new ResponseData<>(HttpStatus.OK.value(), "Cập nhật quyền cho thành viên thành công",
                memberService.updateMemberRoleByRepoIdAndUserId(repositoryId, userId, memberRequest.getRoleId()));
    }

    @DeleteMapping("/{repositoryId}/user/{userId}")
    public ResponseData<Void> removeMemberFromRepository(@PathVariable Long repositoryId, @PathVariable Long userId) {
        memberService.removeMemberByRepoIdAndUserId(repositoryId, userId);
        return new ResponseData<>(HttpStatus.NO_CONTENT.value(), "Xóa thành viên khỏi kho lưu trữ thành công", null);
    }

    @GetMapping("/{repositoryId}")
    public ResponseData<PageResponse<List<MemberResponse>>> getListMember(@PathVariable Long repositoryId, Pageable pageable) {
        return new ResponseData<>(HttpStatus.OK.value(), "Lấy danh sách thành viên thành công",
                memberService.getListMemberByRepoId(repositoryId, pageable));
    }

    @PostMapping("/{repositoryId}/user/{userId}/enable")
    public ResponseData<MemberResponse> enableMember(@PathVariable Long repositoryId, @PathVariable Long userId) {
        return new ResponseData<>(HttpStatus.OK.value(), "Kích hoạt thành viên thành công",
                memberService.enableMemberByRepoIdAndUserId(repositoryId, userId));
    }

    @PostMapping("/{repositoryId}/user/{userId}/disable")
    public ResponseData<MemberResponse> disableMember(@PathVariable Long repositoryId, @PathVariable Long userId) {
        return new ResponseData<>(HttpStatus.OK.value(), "Vô hiệu hóa thành viên thành công",
                memberService.disableMemberByRepoIdAndUserId(repositoryId, userId));
    }

    @PostMapping("/{repositoryId}/leave")
    public ResponseData<MemberResponse> leaveRepo(@PathVariable Long repositoryId) {
        return new ResponseData<>(HttpStatus.OK.value(), "Rời khỏi kho lưu trữ thành công",
                memberService.leaveRepo(repositoryId));
    }
}
