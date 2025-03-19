package vn.kltn.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import vn.kltn.entity.Repo;
import vn.kltn.entity.User;
import vn.kltn.repository.util.RepoUtil;
import vn.kltn.service.IAuthenticationService;
import vn.kltn.service.IMemberService;
import vn.kltn.service.IRepoService;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j(topic = "vn.kltn.aspect.RepoServiceAspect")
public class RepoServiceAspect {




}
