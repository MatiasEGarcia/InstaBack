package com.instaback.dto.request;

import java.io.Serializable;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * @author matia
 * Dto used when user wants to close its session, make its tokens invalidate.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReqLogout implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	@NotBlank(message = "{vali.token-not-blank}")
	private String token;
	
	@NotBlank(message = "{vali.refreshToken-not-blank}")
	private String refreshToken;
}
