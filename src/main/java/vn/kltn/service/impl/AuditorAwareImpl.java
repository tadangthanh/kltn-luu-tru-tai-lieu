package vn.kltn.service.impl;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("auditorProvider")
public class AuditorAwareImpl implements AuditorAware<String> {
    @Override
    public Optional<String> getCurrentAuditor() {
        // Kiểm tra nếu có người dùng xác thực
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            // Trả về giá trị mặc định cho auditor nếu không có người dùng xác thực
            return Optional.of("system");
        }

        // Trả về tên người dùng đã xác thực
        return Optional.of(authentication.getName());
    }
}
