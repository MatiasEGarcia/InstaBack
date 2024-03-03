package com.instaback.config;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class WebSocketEventListener {

	@EventListener
	public void hanldeWebSocketDisconnectListener(SessionDisconnectEvent event) {
		// Handle WebSocket disconnect event
        String sessionId = event.getSessionId();
		log.info("WebSocket session disconnected : {}", sessionId);

	}
}
