package vn.kltn.aspect;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import vn.kltn.common.RepoActionType;
import vn.kltn.common.TokenType;
import vn.kltn.dto.request.RepoRequestDto;
import vn.kltn.dto.response.RepoResponseDto;
import vn.kltn.service.IJwtService;
import vn.kltn.service.IRepoActivityService;

@Aspect
@Component
@RequiredArgsConstructor
public class RepoActivityAspect {
    private final IRepoActivityService repoActivityService;
    private final IJwtService jwtService;

    @Pointcut("execution(* vn.kltn.service.impl.RepoServiceImpl.addMemberToRepository(..))")
    public void addMemberRepoPointCut() {
    }

    @Pointcut("execution(* vn.kltn.service.impl.RepoServiceImpl.createRepository(..))")
    public void createRepoPointCut() {
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


    @AfterReturning(value = "acceptInvitation()")
    public void logMemberAcceptInvitation(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        Long repoId = (Long) args[0];
        String token = (String) args[1];
        String email = jwtService.extractEmail(token, TokenType.INVITATION_TOKEN);
        repoActivityService.logActivity(repoId, email, RepoActionType.MEMBER_ACCEPT_INVITATION,
                String.format("Thành viên user email: #%s tham gia vào repository #%s", email, repoId));
    }

    @AfterReturning(value = "rejectInvitation()")
    public void logMemberRejectInvitation(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        Long repoId = (Long) args[0];
        String email = (String) args[1];
        repoActivityService.logActivity(repoId, email, RepoActionType.MEMBER_REJECT_INVITATION,
                String.format("Thành viên user email: #%s từ chối tham gia vào repository #%s", email, repoId));
    }


    @AfterReturning(value = "updateRepo()")
    public void logUpdateRepo(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        Long repoId = (Long) args[0];
        RepoRequestDto repoRequestDto = (RepoRequestDto) args[1];
        repoActivityService.logActivity(repoId, RepoActionType.UPDATE_REPOSITORY,
                String.format("Cập nhật repository %s, new name: %s, new description: %s", repoId, repoRequestDto.getName(), repoRequestDto.getDescription()));
    }

    @AfterReturning(value = "createRepoPointCut()", returning = "repoCreated")
    public void logCreateRepo(RepoResponseDto repoCreated) {
        repoActivityService.logActivity(repoCreated.getId(), RepoActionType.CREATE_REPOSITORY,
                String.format("Tạo repository #%s", repoCreated.getId()));
    }

    @AfterReturning(value = "addMemberRepoPointCut()")
    public void logAddMember(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        Long repoId = (Long) args[0];
        Long userId = (Long) args[1];
        repoActivityService.logActivity(repoId, RepoActionType.SEND_MEMBER_INVITE, String.format("Thêm thành viên #%s vào repository #%s", userId, repoId));
    }


}
