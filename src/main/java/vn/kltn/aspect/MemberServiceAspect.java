package vn.kltn.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import vn.kltn.common.RepoActionType;
import vn.kltn.dto.response.MemberResponse;
import vn.kltn.service.IRepoActivityService;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j(topic = "vn.kltn.aspect.MemberServiceAspect")
public class MemberServiceAspect {
    private final IRepoActivityService repoActivityService;

    @Pointcut("execution(* vn.kltn.service.impl.MemberServiceImpl.removeMemberById(..))")
    public void removeMemberRepoPointCut() {
    }

    @Pointcut("execution(* vn.kltn.service.impl.MemberServiceImpl.sendInvitationRepo(..))")
    public void sendInvitationRepoPointCut() {
    }

    @Pointcut("execution(* vn.kltn.service.impl.MemberServiceImpl.updateMemberRoleById(..))")
    public void updateMemberRole() {
    }

    @Pointcut("execution(* vn.kltn.service.impl.MemberServiceImpl.disableMemberById(..))")
    public void disableMember() {
    }

    @Pointcut("execution(* vn.kltn.service.impl.MemberServiceImpl.enableMemberById(..))")
    public void enableMember() {
    }

    @Pointcut("execution(* vn.kltn.service.impl.MemberServiceImpl.leaveRepo(..))")
    public void memberLeave() {
    }

    @AfterReturning(value = "sendInvitationRepoPointCut()")
    public void logSendInvitation(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        Long repoId = (Long) args[0];
        Long userId = (Long) args[1];
        repoActivityService.logActivity(repoId, RepoActionType.SEND_MEMBER_INVITE, String.format("Thêm thành viên #%s vào repository", userId));
    }

    @AfterReturning(value = "memberLeave()", returning = "memberResponse")
    public void logLeaveMember(JoinPoint joinPoint, MemberResponse memberResponse) {
        Object[] args = joinPoint.getArgs();
        Long repoId = (Long) args[0];
        Long userId = memberResponse.getUserId();
        repoActivityService.logActivity(repoId, RepoActionType.MEMBER_LEAVE, String.format("Thành viên user #%s rời khỏi repository: ", userId));
    }

    @AfterReturning(value = "removeMemberRepoPointCut()", returning = "memberResponse")
    public void logRemoveMember(MemberResponse memberResponse) {
        repoActivityService.logActivity(memberResponse.getRepoId(), RepoActionType.REMOVE_MEMBER, "Xoá thành viên #%s: " + memberResponse.getId());
    }

    @AfterReturning(value = "updateMemberRole()", returning = "memberResponse")
    public void logUpdateMemberRole(JoinPoint joinPoint, MemberResponse memberResponse) {
        Object[] args = joinPoint.getArgs();
        Long memberId = (Long) args[0];
        repoActivityService.logActivity(memberResponse.getRepoId(),
                RepoActionType.CHANGE_MEMBER_ROLE, String.format("cập nhật quyền cho thành viên #%s trong repository #%s thành #%s",
                        memberId, memberResponse.getRepoId(), memberResponse.getRole()));
    }


    @AfterReturning(value = "enableMember()", returning = "memberResponse")
    public void logEnableMember(MemberResponse memberResponse) {
        Long repoId = memberResponse.getRepoId();
        repoActivityService.logActivity(repoId, RepoActionType.ENABLE_MEMBER, String.format("Kích hoạt lại thành viên user id: #%s (member id: #%s) trong repository #%s", memberResponse.getUserId(), memberResponse.getId(), repoId));
    }

    @AfterReturning(value = "disableMember()", returning = "memberResponse")
    public void logDisableMember(MemberResponse memberResponse) {
        Long repoId = memberResponse.getRepoId();
        repoActivityService.logActivity(repoId, RepoActionType.DISABLE_MEMBER, String.format("Vô hiệu thành viên user id: #%s (member id: #%s) trong repository #%s", memberResponse.getUserId(), memberResponse.getId(), repoId));
    }

}
