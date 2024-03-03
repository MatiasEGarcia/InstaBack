package com.instaback.dto.response;

import java.io.Serializable;

import lombok.Builder;
import lombok.Data;

/**
 * 
 * @author matia
 *	Dto to send auth token for httpRequest and refrehs token to client.
 *
 */
@Data
@Builder
public class ResAuthToken implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String token;
	private String refreshToken;
}
