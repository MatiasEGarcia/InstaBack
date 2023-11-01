package com.instaJava.instaJava.service;

import java.util.UUID;

import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.CacheManager;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import com.instaJava.instaJava.dto.WebSocketAuthInfoDto;
import com.instaJava.instaJava.util.MessagesUtils;

import lombok.RequiredArgsConstructor;
//FALTA TESTEAR TOOODO
@Service
@RequiredArgsConstructor
public class WebSocketServiceImpl implements WebSocketService {
	
	private final CacheManager cacheManager;
	private final MessagesUtils messUtils;

	/**
	 * Method to get webSocket token and put it in webSocketCache.
	 * 
	 * @return UUID token for webSocket connection.
	 */
	@Override
	public WebSocketAuthInfoDto getWebSocketToken() {
		UUID webSocketAuthToken = UUID.randomUUID();
		Cache cache = cacheManager.getCache("webSocketCache");
		WebSocketAuthInfoDto webSocketAuthInfoDto = new WebSocketAuthInfoDto(webSocketAuthToken);
		cache.put(webSocketAuthToken, webSocketAuthInfoDto);
		return webSocketAuthInfoDto;
	}

	/**
	 * Method to get UUID token from the ServerHttpRequest.
	 * 
	 * @param request - request from the client.
	 * @return UUID token from the request, if there is not then null.
	 * @throws IllegalArgumentException  if request param is null.
	 */
	@Override
	public UUID getWebSocketAuthToken(ServerHttpRequest request) {
		if(request == null) throw new IllegalArgumentException(messUtils.getMessage("exception.argument.not.null"));
		
		UUID token = null;
		try {
			token = UUID.fromString(UriComponentsBuilder.fromHttpRequest(request).build().getQueryParams()
					.get("authentication").get(0));
		} catch (NullPointerException e) {
			return null;
		}
		return token;
	}

	/**
	 * Method to get WebSocketAuthInfo object with the cache object obtained using authToken . 
	 * Token evicted after got it from cache.
	 * 
	 * @param authToken - UUID token
	 * @return WebSocketAuthInfoDto with the UUID from cache thanks to the authToken or null if authToken is null,
	 * or if there is not a cache object associated with the authToken given.
	 */
	@Override
	public WebSocketAuthInfoDto getWebSocketAuthInfoFromCache(UUID authToken) {
		if(authToken == null) return null;
		Cache webSocketCache = cacheManager.getCache("webSocketCache");
		ValueWrapper cacheResult = webSocketCache.get(authToken);
		if(cacheResult == null) return null;
		webSocketCache.evict(authToken);
		return (WebSocketAuthInfoDto) cacheResult.get();
	}

}
