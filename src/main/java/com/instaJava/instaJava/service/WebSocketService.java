package com.instaJava.instaJava.service;

import java.util.UUID;

import org.springframework.http.server.ServerHttpRequest;

import com.instaJava.instaJava.dto.WebSocketAuthInfoDto;

public interface WebSocketService {

	/**
	 * Method to get webSocket token and put it in webSocketCache.
	 * 
	 * @return UUID token for webSocket connection.
	 */
	public WebSocketAuthInfoDto getWebSocketToken();

	/**
	 * Method to get UUID token from the ServerHttpRequest.
	 * 
	 * @param request - request from the client.
	 * @return UUID token from the request, if there is not then null.
	 * @throws IllegalArgumentException  if request param is null.
	 */
	public UUID getWebSocketAuthToken(ServerHttpRequest request);
	
	/**
	 * Method to get WebSocketAuthInfo object with the cache object obtained using authToken . 
	 * Token evicted after got it from cache.
	 * 
	 * @param authToken - UUID token
	 * @return WebSocketAuthInfoDto with the UUID from cache thanks to the authToken or null if authToken is null,
	 * or if there is not a cache object associated with the authToken given.
	 */
	public WebSocketAuthInfoDto getWebSocketAuthInfoFromCache(UUID authToken);
}
