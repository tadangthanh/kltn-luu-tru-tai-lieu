package vn.kltn.security;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
public class CustomizeRequestFilter extends OncePerRequestFilter {
    private final IUserService userService;
    private final IJwtService jwtService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        log.info("{} {}", request.getMethod(), request.getRequestURI());
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            authorizationHeader = authorizationHeader.substring(7);
            log.info("Authorization Header: {}", authorizationHeader.substring(0, 20));
            String email = "";
            try {
                email = jwtService.extractEmail(authorizationHeader, ACCESS_TOKEN);
                log.info("Email: {}", email);
            } catch (AccessDeniedException e) {
                log.error("Access denied: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(errorResponse(e.getMessage()));
                return;
            }
            UserDetails userDetails = this.userService.loadUserByUsername(email);
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails,
                    null, userDetails.getAuthorities());
            authToken.setDetails((new WebAuthenticationDetailsSource()).buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
            filterChain.doFilter(request, response);
            return;
        }
        filterChain.doFilter(request, response);
    }

    private String errorResponse(String message) {
        try{
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            errorResponse.setError("Forbidden");
            errorResponse.setMessage(message);
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
        private String error;
        private String message;
    }

}
