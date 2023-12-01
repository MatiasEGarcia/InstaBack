package com.instaJava.instaJava.config.filter;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.instaJava.instaJava.dto.WebSocketAuthInfoDto;
import com.instaJava.instaJava.service.WebSocketService;
import com.instaJava.instaJava.util.MessagesUtils;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WebSocketHandshakeAuthInterceptor implements HandshakeInterceptor {

	private final MessagesUtils messUtils; 
	private final WebSocketService webSocketService;
	
	/**
	 * Will check if the request has the web socket auth token an check if is in the cache, if not return HttpStatus.UNAUTHORIZED
	 * and false, if there is then return true.
	 */
	@Override
	public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
			Map<String, Object> attributes) throws Exception {
		UUID authToken = webSocketService.getWebSocketAuthToken(request);
		WebSocketAuthInfoDto webSocketAuthInfoDto = webSocketService.getWebSocketAuthInfoFromCache(authToken);
		if(webSocketAuthInfoDto == null) {
			response.setStatusCode(HttpStatus.UNAUTHORIZED);
			response.getHeaders().add("X-Unauthorized-Reason", messUtils.getMessage("client-auth-token-invalid")); //I CAN'T SEND A BODY
			return false;
		}
		return true;
	}

	@Override
	public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
			Exception exception) {
		// TODO Auto-generated method stub		
	}
}
