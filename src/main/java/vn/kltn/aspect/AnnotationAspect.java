//package vn.kltn.aspect;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.aspectj.lang.JoinPoint;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.annotation.Before;
//import org.springframework.stereotype.Component;
//import vn.kltn.repository.PermissionRepo;
//import vn.kltn.service.IAuthenticationService;
//
//@Aspect
//@Component
//@RequiredArgsConstructor
//@Slf4j(topic = "vn.kltn.aspect.AnnotationAspect")
//public class AnnotationAspect {
//    private final IAuthenticationService authService;
//    private final PermissionRepo permissionRepo;
//
////    @Before("@annotation(vn.kltn.validation.HasAnyRole)")
////    public void checkPermissionMember(JoinPoint joinPoint) {
////        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
////        Method method = signature.getMethod();
////        HasAnyRole annotation = method.getAnnotation(HasAnyRole.class);
////        RoleName[] listRole = annotation.value();
////
////        Long repoId = repoUtil.getRepoIdByJoinPoint(joinPoint);
////        // Lấy thông tin user
////        User authUser = authService.getCurrentUser();
////        // thành viên có trạng thái ACTIVE và có role trong listRole thì được tiếp tục
////        if (!repoMemberService.userHasAnyRoleRepoId(repoId, authUser.getId(), listRole)) {
////            throw new AccessDeniedException("Bạn không có quyền thực hiện hành động này");
////        }
////    }
//
////    @Before("@annotation(vn.kltn.validation.RequiredEditor)")
////    public void checkRepoMembership(JoinPoint joinPoint) {
////        log.info("Kiểm tra có phải thành viên repo");
////        Long repoId = repoUtil.getRepoIdByJoinPoint(joinPoint);
////        User authUser = authService.getCurrentUser();
////        if (!repoMemberService.isExistMemberActiveByRepoIdAndUserId(repoId, authUser.getId())) {
////            log.error("{} Không phải thành viên repo, repoId: {}", authUser.getEmail(), repoId);
////            throw new AccessDeniedException("Bạn không có quyền thực hiện hành động này");
////        }
////    }
//
//}
