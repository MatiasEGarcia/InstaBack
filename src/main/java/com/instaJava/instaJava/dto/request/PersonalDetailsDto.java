package com.instaJava.instaJava.dto.request;

import java.io.Serializable;

import org.hibernate.validator.constraints.Range;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PersonalDetailsDto implements Serializable{
	
	private static final long serialVersionUID = 1L;

	@NotBlank
	private String name;
	
	@NotBlank
	private String lastname;
	
	@NotNull
	@Range( min = 18, max = 150)
	private byte age;
	
	@NotBlank
	@Email
	private String email;
}
