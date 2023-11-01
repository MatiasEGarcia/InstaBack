package com.instaJava.instaJava.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.instaJava.instaJava.dto.WebSocketAuthInfoDto;
import com.instaJava.instaJava.util.MessagesUtils;

@ExtendWith(MockitoExtension.class)
class WebSocketServiceImplTest {

	@Mock private CacheManager cacheManager;
	@Mock private MessagesUtils messUtils;
	@InjectMocks private WebSocketServiceImpl service;
	
	@Test
	void getWebSocketToken() {
		CaffeineCache webSocketCache = new CaffeineCache("webSocketCache",
				Caffeine.newBuilder().expireAfterWrite(30,TimeUnit.SECONDS).build());
		when(cacheManager.getCache(anyString())).thenReturn(webSocketCache);
		
		assertNotNull(service.getWebSocketToken());
	}

	@Test
	void getWebSocketAuthTokenequestParamNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> service.getWebSocketAuthToken(null));
	}
	
	@Test
	void getWebSocketAuthInfoFromCacheParamAuthTokenNullReturnNull() {
		assertNull(service.getWebSocketAuthInfoFromCache(null));
	}
	
	@Test
	void getWebSocketAuthInfoFromCacheAuthTokenNoExistsReturnNull() {
		UUID authToken = UUID.randomUUID();
		CaffeineCache webSocketCache = new CaffeineCache("webSocketCache",
				Caffeine.newBuilder().expireAfterWrite(30,TimeUnit.SECONDS).build());
		when(cacheManager.getCache("webSocketCache")).thenReturn(webSocketCache);
	
		assertNull(service.getWebSocketAuthInfoFromCache(authToken));
	}
	
	@Test
	void getWebSocketAuthInfoFromCacheAuthTokenExistsReturnNotNull() {
		UUID authToken = UUID.randomUUID();
		CaffeineCache webSocketCache = new CaffeineCache("webSocketCache",
				Caffeine.newBuilder().expireAfterWrite(30,TimeUnit.SECONDS).build());
		webSocketCache.put(authToken, new WebSocketAuthInfoDto(authToken));
		when(cacheManager.getCache("webSocketCache")).thenReturn(webSocketCache);
	
		assertNotNull(service.getWebSocketAuthInfoFromCache(authToken));
		
		assertNull(service.getWebSocketAuthInfoFromCache(authToken),
				"token should be evicted in the previous request, so if I do the same "
				+ "request there shouldn't exists a cache with the same authToken");
	}
		
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
