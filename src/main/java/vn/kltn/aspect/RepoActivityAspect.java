package vn.kltn.aspect;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import vn.kltn.common.RepoAction;
import vn.kltn.common.RepoPermission;
import vn.kltn.dto.request.RepoRequestDto;
import vn.kltn.exception.InvalidDataException;
import vn.kltn.service.IRepoActivityService;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Aspect
@Component
@RequiredArgsConstructor
public class RepoActivityAspect {
    private final IRepoActivityService repoActivityService;

    @Pointcut("execution(* vn.kltn.service.impl.RepoServiceImpl.addMemberToRepository(..))")
    public void addMemberRepoPointCut() {
    }

    @Pointcut("execution(* vn.kltn.service.impl.RepoServiceImpl.removeMemberByRepoIdAndUserId(..))")
    public void removeMemberRepoPointCut() {
    }

    @Pointcut("execution(* vn.kltn.service.impl.RepoServiceImpl.updatePermissionMemberByRepoIdAndUserId(..))")
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

    @AfterReturning(value = "updateRepo()")
    public void logUpdateRepo(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        Long repoId = (Long) args[0];
        RepoRequestDto repoRequestDto = (RepoRequestDto) args[1];
        repoActivityService.logActivity(repoId, RepoAction.UPDATE_REPOSITORY,
                String.format("Cập nhật repository %s, new name: %s, new description: %s", repoId, repoRequestDto.getName(), repoRequestDto.getDescription()));
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
