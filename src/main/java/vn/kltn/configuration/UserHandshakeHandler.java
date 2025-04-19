package vn.kltn.configuration;
import com.sun.security.auth.UserPrincipal;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

@Component
@NoArgsConstructor
public class UserHandshakeHandler extends DefaultHandshakeHandler {
    protected Principal determineUser(@NonNull ServerHttpRequest request, @NonNull WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (request == null) {
            throw new NullPointerException("request is marked non-null but is null");
        } else if (wsHandler == null) {
            throw new NullPointerException("wsHandler is marked non-null but is null");
        } else {
            return new UserPrincipal(attributes.get("email").toString());
        }
    }
}

