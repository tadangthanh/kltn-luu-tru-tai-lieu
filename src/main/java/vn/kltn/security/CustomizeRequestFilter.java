package vn.kltn.security;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.kltn.service.IJwtService;
import vn.kltn.service.IUserService;

import java.io.IOException;
import java.util.Date;

import static vn.kltn.common.TokenType.ACCESS_TOKEN;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "CUSTOMIZE_REQUEST_FILTER")
@EnableMethodSecurity
public class CustomizeRequestFilter extends OncePerRequestFilter {
    private final IUserService userService;
    private final IJwtService jwtService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        log.info("{} {}", request.getMethod(), request.getRequestURI());

        String token = null;

        // Ưu tiên lấy token từ Header Authorization
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);
            log.info("Lấy token từ Authorization Header");
        } else {
            //  Nếu không có, lấy từ cookie
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("accessToken".equals(cookie.getName())) {
                        token = cookie.getValue();
                        log.info(" Lấy token từ Cookie");
                        break;
                    }
                }
            }
        }

        if (token != null) {
            log.info("Authorization Token: {}", token.substring(0, Math.min(token.length(), 20))); // Log 20 ký tự đầu

            try {
                String email = jwtService.extractEmail(token, ACCESS_TOKEN);
                log.info("User Email: {}", email);
                UserDetails userDetails = this.userService.loadUserByUsername(email);

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } catch (AccessDeniedException e) {
                log.error("Access denied: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(errorResponse(request.getRequestURI(), e.getMessage()));
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String errorResponse(String uri,String message) {
        try{
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            errorResponse.setError("Forbidden");
            errorResponse.setMessage(message);
            errorResponse.setPath(uri);
            errorResponse.setTimestamp(new Date());
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            return gson.toJson(errorResponse);
        }catch (Exception e){
            log.error("Error response: {}", e.getMessage());
           return "";
        }
    }
    @Getter
    @Setter
    private static class ErrorResponse{
        private Date timestamp;
        private int status;
        private String path;
        private String error;
        private String message;
    }

}
