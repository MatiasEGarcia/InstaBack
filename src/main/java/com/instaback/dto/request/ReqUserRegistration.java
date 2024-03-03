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
 * Dto used to register a user.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ReqUserRegistration implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	
	@NotBlank(message="{vali.username-not-blank}")
	private String username;
	
	@NotBlank(message = "{vali.password-not-blank}")
	private String password;
}
