package vn.kltn.configuration;


import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;
import vn.kltn.common.TokenType;
import vn.kltn.service.IJwtService;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements HandshakeInterceptor {
    private final IJwtService jwtService;


    @Override
    public boolean beforeHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response,
                                   @NonNull WebSocketHandler wsHandler, @NonNull Map<String, Object> attributes) {
        List<String> queryParams = UriComponentsBuilder.fromUri(request.getURI()).build().getQueryParams().get("token");
        String jwtToken = (queryParams != null && !queryParams.isEmpty()) ? queryParams.get(0) : null;
        if (jwtToken == null) return false;
        String email = jwtService.extractEmail(jwtToken, TokenType.ACCESS_TOKEN);
        if (email != null && jwtService.isTokenValid(jwtToken, TokenType.ACCESS_TOKEN)) {
            attributes.put("email", email);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void afterHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response,
                               @NonNull WebSocketHandler wsHandler, Exception exception) {

    }
}
