package com.instaJava.instaJava.dto.request;

import java.io.Serializable;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ReqUserRegistration implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	@NotBlank
	private String username;
	
	@NotBlank
	private String password;
}
