package com.instaback.config.filter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;

import com.instaback.dto.WebSocketAuthInfoDto;
import com.instaback.service.WebSocketService;
import com.instaback.util.MessagesUtils;

@ExtendWith(MockitoExtension.class)
class WebSocketHandshakeAuthInterceptorTest {

	@Mock private MessagesUtils messUtils;
	@Mock private WebSocketService webSocketService;
	@Mock private ServerHttpRequest request;
	@Mock private ServerHttpResponse response;
	@Mock private WebSocketHandler webSocketHandler;
	@InjectMocks private WebSocketHandshakeAuthInterceptor interceptor;
	private final Map<String, Object> attributes = new HashMap<>();
	
	
	@Test
	void beforeHandshakeWithoutTokenResponseUnauthorizedReturnsFalse() throws Exception {
		UUID token = UUID.randomUUID();
		when(webSocketService.getWebSocketAuthToken(request)).thenReturn(token);
		when(webSocketService.getWebSocketAuthInfoFromCache(token)).thenReturn(null);//that cache key don't exists 
	    // Mock the headers of the response
	    HttpHeaders headers = new HttpHeaders();	
	    when(response.getHeaders()).thenReturn(headers);
	    
		boolean result = interceptor.beforeHandshake(request, response, webSocketHandler, attributes);
		
		assertFalse(result, "There is not authToken so should return false");
		verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
		assertTrue(response.getHeaders().containsKey("X-Unauthorized-Reason"), "Response should have X-Unauthorized-Reason");
	}
	
	@Test
	void beforeHandshakeWithTokenAndCacheExistsReturnsTrue() throws Exception {
		UUID token = UUID.randomUUID();
		when(webSocketService.getWebSocketAuthToken(request)).thenReturn(token);
		when(webSocketService.getWebSocketAuthInfoFromCache(token)).thenReturn(new WebSocketAuthInfoDto());//that cache key don't exists 
	    
		boolean result = interceptor.beforeHandshake(request, response, webSocketHandler, attributes);
		
		assertTrue(result, "There is authToken and cache exists so should return true");
		verify(response,never()).setStatusCode(HttpStatus.UNAUTHORIZED);
	}
}
