package vn.kltn.aspect;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import vn.kltn.common.RepoAction;
import vn.kltn.common.RepoPermission;
import vn.kltn.dto.response.RepoResponseDto;
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

    @Pointcut("execution(* vn.kltn.service.impl.RepoServiceImpl.removeMemberFromRepository(..))")
    public void removeMemberRepoPointCut() {
    }

    @Pointcut("execution(* vn.kltn.service.impl.RepoServiceImpl.updatePermissionMember(..))")
    public void updatePermissionMember() {
    }

    @AfterReturning(value = "addMemberRepoPointCut()", returning = "repoResponseDto")
    public void logAddMember(JoinPoint joinPoint, RepoResponseDto repoResponseDto) {
        Object[] args = joinPoint.getArgs();
        Long repoId = (Long) args[0];
        Long userId = (Long) args[1];
        repoActivityService.logActivity(repoId, RepoAction.ADD_MEMBER, "Thêm thành viên vào repository: " + repoResponseDto.getName() + ", userId: " + userId);
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
                "Cập nhật quyền hạn cho thành viên memberId: " + memberId + ", new permissions: " + requestedPermissions);
    }

}
