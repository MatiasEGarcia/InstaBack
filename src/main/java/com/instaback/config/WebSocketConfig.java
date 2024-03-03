package com.instaback.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.instaback.config.filter.WebSocketHandshakeAuthInterceptor;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer{

	private final WebSocketHandshakeAuthInterceptor webSocketHandshakeAuthInterceptor;

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/ws/connect")//starter path for all webSocket connections
				.addInterceptors(webSocketHandshakeAuthInterceptor)
				.setAllowedOriginPatterns("http://127.0.0.1:5173")//react app origin
				.withSockJS();
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry.setApplicationDestinationPrefixes("/api/v1/notifications");//I think that when I'll add a chat should add another prefixe like chatroom or somehting like that.
		registry.enableSimpleBroker("/user","/notifications","/chat");
		registry.setUserDestinationPrefix("/user/notifications");
	}

}
