package vn.kltn.aspect;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class RepoActivityAspect {
//    private final IRepoActivityService repoActivityService;
//    private final IJwtService jwtService;
//
//
//    @Pointcut("execution(* vn.kltn.service.impl.RepoServiceImpl.createRepository(..))")
//    public void createRepoPointCut() {
//    }
//
//
//    @Pointcut("execution(* vn.kltn.service.impl.RepoServiceImpl.acceptInvitation(..))")
//    public void acceptInvitation() {
//    }
//
//    @Pointcut("execution(* vn.kltn.service.impl.RepoServiceImpl.rejectInvitation(..))")
//    public void rejectInvitation() {
//    }
//
//    @Pointcut("execution(* vn.kltn.service.impl.RepoServiceImpl.update(..))")
//    public void updateRepo() {
//    }
//
//
//    @AfterReturning(value = "acceptInvitation()")
//    public void logMemberAcceptInvitation(JoinPoint joinPoint) {
//        Object[] args = joinPoint.getArgs();
//        Long repoId = (Long) args[0];
//        String token = (String) args[1];
//        String email = jwtService.extractEmail(token, TokenType.INVITATION_TOKEN);
//        repoActivityService.logActivity(repoId, email, RepoActionType.MEMBER_ACCEPT_INVITATION,
//                String.format("Thành viên user email: #%s tham gia vào repository", email));
//    }
//
//    @AfterReturning(value = "rejectInvitation()")
//    public void logMemberRejectInvitation(JoinPoint joinPoint) {
//        Object[] args = joinPoint.getArgs();
//        Long repoId = (Long) args[0];
//        String email = (String) args[1];
//        repoActivityService.logActivity(repoId, email, RepoActionType.MEMBER_REJECT_INVITATION,
//                String.format("Thành viên user email: #%s từ chối lời mời", email));
//    }
//
//
//    @AfterReturning(value = "updateRepo()")
//    public void logUpdateRepo(JoinPoint joinPoint) {
//        Object[] args = joinPoint.getArgs();
//        Long repoId = (Long) args[0];
//        repoActivityService.logActivity(repoId, RepoActionType.UPDATE_REPOSITORY,
//                "Cập nhật repository");
//    }
//
//    @AfterReturning(value = "createRepoPointCut()", returning = "repoCreated")
//    public void logCreateRepo(RepoResponseDto repoCreated) {
//        repoActivityService.logActivity(repoCreated.getId(), RepoActionType.CREATE_REPOSITORY,
//                String.format("Tạo repository #%s", repoCreated.getId()));
//    }
//

}
