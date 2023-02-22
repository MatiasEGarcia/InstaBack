package com.instaJava.instaJava.dto.response;

import java.io.Serializable;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResAuthToken implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String token;
	private String refreshToken;
}
