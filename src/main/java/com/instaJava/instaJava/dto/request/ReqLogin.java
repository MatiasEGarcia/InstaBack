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
public class ReqLogin implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	@NotBlank(message= "{vali.username-not-blank}")
	private String username;
	
	@NotBlank(message = "{vali.password-not-blank}")
	private String password;
}
