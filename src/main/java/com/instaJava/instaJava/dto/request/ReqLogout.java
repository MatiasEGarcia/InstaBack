package com.instaJava.instaJava.dto.request;

import java.io.Serializable;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class ReqLogout implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	@NotBlank
	private String token;
	
	@NotBlank
	private String refreshToken;
}
