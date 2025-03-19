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

    @Pointcut("execution(* vn.kltn.service.impl.MemberServiceImpl.removeMemberByRepoIdAndUserId(..))")
    public void removeMemberRepoPointCut() {
    }

    @Pointcut("execution(* vn.kltn.service.impl.MemberServiceImpl.updateMemberRoleByRepoIdAndUserId(..))")
    public void updateMemberRole() {
    }

    @Pointcut("execution(* vn.kltn.service.impl.MemberServiceImpl.disableMemberByRepoIdAndUserId(..))")
    public void disableMember() {
    }

    @Pointcut("execution(* vn.kltn.service.impl.MemberServiceImpl.enableMemberByRepoIdAndUserId(..))")
    public void enableMember() {
    }

    @Pointcut("execution(* vn.kltn.service.impl.MemberServiceImpl.leaveRepo(..))")
    public void memberLeave() {
    }

    @AfterReturning(value = "memberLeave()", returning = "memberResponse")
    public void logLeaveMember(JoinPoint joinPoint, MemberResponse memberResponse) {
        Object[] args = joinPoint.getArgs();
        Long repoId = (Long) args[0];
        Long userId = memberResponse.getUserId();
        repoActivityService.logActivity(repoId, RepoActionType.MEMBER_LEAVE,  String.format("Thành viên user #%s rời khỏi repository: ", userId));
    }

    @AfterReturning(value = "removeMemberRepoPointCut()")
    public void logRemoveMember(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        Long repoId = (Long) args[0];
        Long memberId = (Long) args[1];
        repoActivityService.logActivity(repoId, RepoActionType.REMOVE_MEMBER, "Xoá thành viên #%s: " + memberId);
    }

    @AfterReturning(value = "updateMemberRole()", returning = "memberResponse")
    public void logUpdateMemberRole(JoinPoint joinPoint, MemberResponse memberResponse) {
        Object[] args = joinPoint.getArgs();
        Long repoId = (Long) args[0];
        Long memberId = (Long) args[1];
        repoActivityService.logActivity(repoId, RepoActionType.CHANGE_MEMBER_ROLE, String.format("cập nhật quyền cho thành viên #%s trong repository #%s thành #%s", memberId, repoId, memberResponse.getRole()));
    }

    @AfterReturning(value = "enableMember()", returning = "memberResponse")
    public void logMemberLeave(JoinPoint joinPoint, MemberResponse memberResponse) {
        Object[] args = joinPoint.getArgs();
        Long repoId = (Long) args[0];
        repoActivityService.logActivity(repoId, RepoActionType.MEMBER_LEAVE, String.format("Thành viên user id: #%s (member id: #%s) rời khỏi repository #%s", memberResponse.getUserId(), memberResponse.getId(), repoId));
    }

    @AfterReturning(value = "enableMember()", returning = "memberResponse")
    public void logEnableMember(JoinPoint joinPoint, MemberResponse memberResponse) {
        Object[] args = joinPoint.getArgs();
        Long repoId = (Long) args[0];
        repoActivityService.logActivity(repoId, RepoActionType.ENABLE_MEMBER, String.format("Kích hoạt lại thành viên user id: #%s (member id: #%s) trong repository #%s", memberResponse.getUserId(), memberResponse.getId(), repoId));
    }

    @AfterReturning(value = "disableMember()", returning = "memberResponse")
    public void logDisableMember(JoinPoint joinPoint, MemberResponse memberResponse) {
        Object[] args = joinPoint.getArgs();
        Long repoId = (Long) args[0];
        repoActivityService.logActivity(repoId, RepoActionType.DISABLE_MEMBER, String.format("Vô hiệu thành viên user id: #%s (member id: #%s) trong repository #%s", memberResponse.getUserId(), memberResponse.getId(), repoId));
    }

}
