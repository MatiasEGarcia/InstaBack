package com.instaJava.instaJava.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class WebSocketAuthInfoDto {
	
	private UUID webSocketAuthToken;
}
