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
import vn.kltn.validation.Create;
import vn.kltn.validation.Update;

import java.util.List;

@RequiredArgsConstructor
@RestController
@Validated
@RequestMapping("/api/v1/member")
public class MemberRest {
    private final IMemberService memberService;

    @PostMapping("/{memberId}")
    public ResponseData<MemberResponse> updatePermissionForMember(@PathVariable Long memberId,
                                                                  @Validated(Update.class) @RequestBody MemberRequest memberRequest) {
        return new ResponseData<>(HttpStatus.OK.value(), "Cập nhật quyền cho thành viên thành công",
                memberService.updateMemberRoleById(memberId, memberRequest.getRoleId()));
    }

    @PostMapping("/{repositoryId}/invitation")
    public ResponseData<MemberResponse> sendInvitation(@PathVariable Long repositoryId,
                                                       @Validated(Create.class) @RequestBody MemberRequest memberRequest) {
        return new ResponseData<>(HttpStatus.CREATED.value(), "gửi lơi mời thành công",
                memberService.sendInvitationRepo(repositoryId, memberRequest.getUserId(), memberRequest.getRoleId()));
    }

    @DeleteMapping("/{memberId}")
    public ResponseData<Void> removeMemberFromRepository(@PathVariable Long memberId) {
        memberService.removeMemberById(memberId);
        return new ResponseData<>(HttpStatus.NO_CONTENT.value(), "Xóa thành viên khỏi kho lưu trữ thành công", null);
    }

    @GetMapping("/{repositoryId}")
    public ResponseData<PageResponse<List<MemberResponse>>> getListMember(@PathVariable Long repositoryId, Pageable pageable) {
        return new ResponseData<>(HttpStatus.OK.value(), "Lấy danh sách thành viên thành công",
                memberService.getListMemberByRepoId(repositoryId, pageable));
    }

    @PostMapping("/{memberId}/enable")
    public ResponseData<MemberResponse> enableMember(@PathVariable Long memberId) {
        return new ResponseData<>(HttpStatus.OK.value(), "thành công",
                memberService.enableMemberById(memberId));
    }

    @PostMapping("/{memberId}/disable")
    public ResponseData<MemberResponse> disableMember(@PathVariable Long memberId) {
        return new ResponseData<>(HttpStatus.OK.value(), "thành công",
                memberService.disableMemberById(memberId));
    }

    @PostMapping("/{repositoryId}/leave")
    public ResponseData<MemberResponse> leaveRepo(@PathVariable Long repositoryId) {
        return new ResponseData<>(HttpStatus.OK.value(), "Rời khỏi kho lưu trữ thành công",
                memberService.leaveRepo(repositoryId));
    }
}
