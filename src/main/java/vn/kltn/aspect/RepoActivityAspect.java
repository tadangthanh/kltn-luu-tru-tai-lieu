package vn.kltn.aspect;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import vn.kltn.common.RepoAction;
import vn.kltn.common.RepoPermission;
import vn.kltn.common.TokenType;
import vn.kltn.dto.request.RepoRequestDto;
import vn.kltn.dto.response.RepoResponseDto;
import vn.kltn.exception.InvalidDataException;
import vn.kltn.service.IJwtService;
import vn.kltn.service.IRepoActivityService;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Aspect
@Component
@RequiredArgsConstructor
public class RepoActivityAspect {
    private final IRepoActivityService repoActivityService;
    private final IJwtService jwtService;

    @Pointcut("execution(* vn.kltn.service.impl.RepoServiceImpl.addMemberToRepository(..))")
    public void addMemberRepoPointCut() {
    }

    @Pointcut("execution(* vn.kltn.service.impl.RepoServiceImpl.removeMemberFromRepository(..))")
    public void removeMemberRepoPointCut() {
    }

    @Pointcut("execution(* vn.kltn.service.impl.RepoServiceImpl.updatePermissionMember(..))")
    public void updatePermissionMember() {
    }

    @Pointcut("execution(* vn.kltn.service.impl.RepoServiceImpl.acceptInvitation(..))")
    public void acceptInvitation() {
    }

    @Pointcut("execution(* vn.kltn.service.impl.RepoServiceImpl.rejectInvitation(..))")
    public void rejectInvitation() {
    }

    @Pointcut("execution(* vn.kltn.service.impl.RepoServiceImpl.update(..))")
    public void updateRepo() {
    }

    @AfterReturning(value = "updateRepo()", returning = "repoResponseDto")
    public void logUpdateRepo(JoinPoint joinPoint, RepoResponseDto repoResponseDto) {
        Object[] args = joinPoint.getArgs();
        Long repoId = (Long) args[0];
        RepoRequestDto repoRequestDto = (RepoRequestDto) args[1];
        repoActivityService.logActivity(repoId, RepoAction.UPDATE_REPOSITORY,
                String.format("Cập nhật repository %s, new name: %s, new description: %s", repoId, repoRequestDto.getName(), repoRequestDto.getDescription()));
    }

    @AfterReturning(value = "rejectInvitation()")
    public void logRejectInvitation(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        Long repoId = (Long) args[0];
        String email = (String) args[1];
        repoActivityService.logActivity(repoId, RepoAction.MEMBER_REJECT_INVITATION,
                String.format("Từ chối lời mời tham gia repository %s từ email: %s", repoId, email));
    }

    @AfterReturning(value = "acceptInvitation()")
    public void logAcceptInvitation(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        Long repoId = (Long) args[0];
        String token = (String) args[1];
        String email = jwtService.extractEmail(token, TokenType.INVITATION_TOKEN);
        repoActivityService.logActivity(repoId, RepoAction.MEMBER_ACCEPT_INVITATION,
                String.format("Chấp nhận lời mời tham gia repository %s từ email: %s", repoId, email));
    }

    @AfterReturning(value = "addMemberRepoPointCut()")
    public void logAddMember(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        Long repoId = (Long) args[0];
        Long userId = (Long) args[1];
        repoActivityService.logActivity(repoId, RepoAction.ADD_MEMBER, String.format("Thêm thành viên %s vào repository %s", userId, repoId));
    }

    @AfterReturning(value = "removeMemberRepoPointCut()")
    public void logRemoveMember(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        Long repoId = (Long) args[0];
        Long memberId = (Long) args[1];
        repoActivityService.logActivity(repoId, RepoAction.REMOVE_MEMBER, "Xoá viên memberId: " + memberId);
    }

    @AfterReturning(value = "updatePermissionMember()")
    public void logUpdatePermissionMember(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        Long repoId = (Long) args[0];
        Long memberId = (Long) args[1];
        Set<RepoPermission> requestedPermissions = new HashSet<>();
        if (args[2] instanceof Collection<?>) { // Kiểm tra nếu là Collection (Set, List, ...)
            for (Object obj : (Collection<?>) args[2]) {
                if (obj instanceof RepoPermission) {
                    requestedPermissions.add((RepoPermission) obj);
                } else {
                    throw new InvalidDataException("Invalid element type in requestedPermissions: " + obj.getClass());
                }
            }
        } else {
            throw new InvalidDataException("Expected a Set<RepoPermission>, but got: " + args[2].getClass());
        }
        repoActivityService.logActivity(repoId, RepoAction.UPDATE_PERMISSION,
                String.format("Cập nhật quyền hạn cho thành viên memberId: %s, new permissions: %s", memberId, requestedPermissions));
    }

}
