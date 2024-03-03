package com.instaback.service;

import java.util.UUID;

import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.CacheManager;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import com.instaback.dto.WebSocketAuthInfoDto;
import com.instaback.util.MessagesUtils;

import lombok.RequiredArgsConstructor;
//FALTA TESTEAR TOOODO
@Service
@RequiredArgsConstructor
public class WebSocketServiceImpl implements WebSocketService {
	
	private final CacheManager cacheManager;
	private final MessagesUtils messUtils;

	
	@Override
	public WebSocketAuthInfoDto getWebSocketToken() {
		UUID webSocketAuthToken = UUID.randomUUID();
		Cache cache = cacheManager.getCache("webSocketCache");
		WebSocketAuthInfoDto webSocketAuthInfoDto = new WebSocketAuthInfoDto(webSocketAuthToken);
		cache.put(webSocketAuthToken, webSocketAuthInfoDto);
		return webSocketAuthInfoDto;
	}

	
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
