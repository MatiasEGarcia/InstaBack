package com.instaJava.instaJava.dto.request;

import java.io.Serializable;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReqRefreshToken implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	@NotBlank(message = "{vali.token-not-blank}")
	private String token;
	
	@NotBlank(message = "{vali.refreshToken-not-blank}")
	private String refreshToken;
}
