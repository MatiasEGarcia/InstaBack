package com.instaback.dto;

import java.io.Serializable;

import org.hibernate.validator.constraints.Range;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * @author matia
 * Dto to create PersonalDetails records. It doesn't have user information because user
 * should have been authenticated , we get user information from there.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PersonalDetailsDto implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private String id;

	@NotBlank(message = "{vali.name-not-blank}")
	private String name;
	
	@NotBlank(message = "{vali.lastname-not-blank}")
	private String lastname;
	
	@NotNull(message = "{vali.age-not-null}")
	@Range( min = 18, max = 150 , message = "{vali.age-range}")
	private byte age;
	
	@NotBlank(message = "{vali.email-not-blank}")
	@Email(message = "{vali.email-email}")
	private String email;
}
