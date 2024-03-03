package com.instaback.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * @author matia
 * Dto to return {@link com.instaback.entity.User} data to the client.
 *
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserDto implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private String id;
	
	private String username;
	
	private String image;
	
	private boolean visible;
	
	private boolean admin;//for chat purposes
	
	public UserDto(String userId) {
		this.id = userId;
	}
}
