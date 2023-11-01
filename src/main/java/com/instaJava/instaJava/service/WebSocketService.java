package com.instaJava.instaJava.service;

import java.util.UUID;

import org.springframework.http.server.ServerHttpRequest;

import com.instaJava.instaJava.dto.WebSocketAuthInfoDto;

public interface WebSocketService {

	public WebSocketAuthInfoDto getWebSocketToken();

	public UUID getWebSocketAuthToken(ServerHttpRequest request);
	
	public WebSocketAuthInfoDto getWebSocketAuthInfoFromCache(UUID authToken);
}
