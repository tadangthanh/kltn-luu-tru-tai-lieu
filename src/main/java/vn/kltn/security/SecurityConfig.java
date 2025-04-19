package vn.kltn.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import vn.kltn.dto.response.TokenResponse;
import vn.kltn.service.IUserService;

import java.util.List;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@RequiredArgsConstructor
@EnableMethodSecurity
@EnableWebSecurity
public class SecurityConfig implements WebMvcConfigurer {
    private final IUserService userDetailsService;
    private final CustomizeRequestFilter requestFilter;
    private final PasswordEncoder passwordEncoder;
    private final CustomAuthorizationRequestRepository authorizationRequestRepository;
    private final String[] WHITE_LIST = {
            "/auth/**",
            "/api/auth/**",
            "/api/v1/user/register",
            "/api/v1/user/confirm/**",
            "/api/v1/user/re-confirm",
            "/api/v1/user/forgot-password",
            "/api/v1/user/reset-password",
            "/repository/invitation/**"
    };

    //quan ly cac roles, user truy cap he thong
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));
        configuration.setAllowCredentials(true);
        configuration.setAllowedMethods(List.of("*"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        source.registerCorsConfiguration("/ws/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable) //mat giao dien login
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers(WHITE_LIST).permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/v1/file/view/*").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/v1/file/download/*").permitAll()
                                .anyRequest().authenticated()
                ).sessionManagement(manager -> manager.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(provider()).addFilterBefore(requestFilter, UsernamePasswordAuthenticationFilter.class);
//                .oauth2Login(
//                        oauth2Login -> oauth2Login
//                                .authorizationEndpoint(authEndpoint -> authEndpoint
//                                        .authorizationRequestRepository(authorizationRequestRepository)
//                                )
//                                .successHandler((request, response, authentication) -> {
//                                    // Ép kiểu authentication.getPrincipal() thành OAuth2User
//                                    OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
//                                    TokenResponse tokenResponse = userDetailsService.loginWithGoogle(oAuth2User);
//                                    // Tạo cookie cho Access Token (ngắn hạn, ví dụ: 15 phút)
//                                    Cookie accessTokenCookie = createCookie("accessToken", tokenResponse.getAccessToken(), 900); // 900 giây = 15 phút
//                                    // Tạo cookie cho Refresh Token (dài hạn, ví dụ: 7 ngày)
//                                    Cookie refreshTokenCookie = createCookie("refreshToken", tokenResponse.getRefreshToken(), 604800); // 604800 giây = 7 ngày
//                                    // Thêm cookie vào response
//                                    response.addCookie(accessTokenCookie);
//                                    response.addCookie(refreshTokenCookie);
//
//                                    // Lấy redirectUrl từ cookie để chuyển tiếp nếu login thành công
//                                    String redirectUrl = getDirectUrl(request);
//
//                                    // Chuyển hướng về URL gốc (hoặc trang chủ nếu không có URL gốc)
//                                    response.sendRedirect(redirectUrl != null ? redirectUrl : "http://localhost:3000");
//
//                                })
//                );
        http.httpBasic(AbstractHttpConfigurer::disable);
        http.cors((cors) -> {
            cors.configurationSource(this.corsConfigurationSource());
        });
        return http.build();
    }

    // cung cap truy cap toi tang dao
    @Bean
    public AuthenticationProvider provider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    private String getDirectUrl(HttpServletRequest request) {
        // Lấy redirect_uri từ cookie nếu có
        String redirectUrl = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("redirectUrl".equals(cookie.getName())) {
                    redirectUrl = cookie.getValue();
                    break;
                }
            }
        }
        return redirectUrl;
    }

    private Cookie createCookie(String name, String value, int maxAgeSeconds) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(maxAgeSeconds);
        return cookie;
    }

}
