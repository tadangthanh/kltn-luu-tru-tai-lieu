package vn.kltn.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {
    private static final String REDIRECT_URI_SESSION_ATTRIBUTE = "redirectUrl";

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        return (OAuth2AuthorizationRequest) request.getSession()
                .getAttribute("oauth2_auth_request");
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
                                         HttpServletRequest request, HttpServletResponse response) {
        if (authorizationRequest != null) {
            request.getSession().setAttribute("oauth2_auth_request", authorizationRequest);
            // Lưu URL gốc vào session
            String redirectUrl = request.getParameter("redirectUrl");
            if (redirectUrl != null) {
                // Lưu redirectUrl vào cookie
                Cookie redirectCookie = new Cookie(REDIRECT_URI_SESSION_ATTRIBUTE, redirectUrl);
                redirectCookie.setHttpOnly(true);
                redirectCookie.setPath("/");
                redirectCookie.setMaxAge(300); // 5 phút
                response.addCookie(redirectCookie);
            }
        }
    }
    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession();
        OAuth2AuthorizationRequest authRequest =
                (OAuth2AuthorizationRequest) session.getAttribute("oauth2_auth_request");
        session.removeAttribute("oauth2_auth_request");
        session.removeAttribute(REDIRECT_URI_SESSION_ATTRIBUTE); // Xóa URL gốc khi hoàn tất
        return authRequest;
    }

}