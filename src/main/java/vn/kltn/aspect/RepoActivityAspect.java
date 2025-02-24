package vn.kltn.aspect;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import vn.kltn.common.RepoAction;
import vn.kltn.dto.response.RepoResponseDto;
import vn.kltn.service.IRepoActivityService;

@Aspect
@Component
@RequiredArgsConstructor
public class RepoActivityAspect {
    private final IRepoActivityService repoActivityService;

    @Pointcut("execution(* vn.kltn.service.impl.RepoServiceImpl.addMemberToRepository(..))")
    public void addMemberRepositoryPointCut() {
    }

    @AfterReturning(value = "addMemberRepositoryPointCut()", returning = "repoResponseDto")
    public void logAddFile(JoinPoint joinPoint, RepoResponseDto repoResponseDto) {
        Object[] args = joinPoint.getArgs();
        Long repoId = (Long) args[0];
        repoActivityService.logActivity(repoId, RepoAction.ADD_MEMBER, "Thêm thành viên vào repository: " + repoResponseDto.getName());
    }
}
