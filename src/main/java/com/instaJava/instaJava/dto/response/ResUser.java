package com.instaJava.instaJava.dto.response;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * @author matia
 * Dto to return {@link com.instaJava.instaJava.entity.User} data to the client.
 *
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ResUser implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private String userId;
	
	private String username;
	
	private String image;
	
	private boolean visible;
}
