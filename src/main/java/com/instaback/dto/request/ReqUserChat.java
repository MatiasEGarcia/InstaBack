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
 *	Request object to add user on chat.
 * Username to identify user. and admin to know if should be admin or not.
 *
 */

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ReqUserChat implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	@NotBlank(message = "{vali.username-not-blank}")
	private String username;
	private boolean admin;//default false.
}
