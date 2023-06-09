package com.instaJava.instaJava.dto.request;

import java.io.Serializable;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * @author matia
 *When authentication token is expired, user use this Dto to refresh them.
 *
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ReqRefreshToken implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	@NotBlank(message = "{vali.token-not-blank}")
	private String token;
	
	@NotBlank(message = "{vali.refreshToken-not-blank}")
	private String refreshToken;
}
