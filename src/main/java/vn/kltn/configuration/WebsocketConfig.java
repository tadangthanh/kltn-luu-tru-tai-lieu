package vn.kltn.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebsocketConfig implements WebSocketMessageBrokerConfigurer {
    private final UserHandshakeHandler userHandshakeHandler;
    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
    // Khi bạn sử dụng enableSimpleBroker("/topic"), nó cho phép server gửi tin nhắn đến tất cả các client đã đăng ký vào destination bắt đầu bằng /topic
    // Nếu một client đăng ký vào /topic/news, bất kỳ tin nhắn nào được gửi đến destination đó sẽ được tự động gửi đến tất cả các client đã đăng ký
        config.enableSimpleBroker("/topic");
    //Khi client gửi tin nhắn đến server, nó sẽ sử dụng các destination bắt đầu với những tiền tố này
        config.setApplicationDestinationPrefixes("/app");
        //Đặt tiền tố cho các destination mà các tin nhắn sẽ được gửi đến người dùng cụ thể.
        //Khi bạn sử dụng convertAndSendToUser(userId, "/location", message), nó sẽ gửi tin nhắn đến một user cụ thể.
        //Server sẽ tạo ra destination cho người dùng có ID là userId, và địa chỉ đó sẽ là /user/userId/location
        //config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*")
                .addInterceptors(webSocketAuthInterceptor)
                .setHandshakeHandler(userHandshakeHandler).withSockJS();
    }

}
