package com.instaJava.instaJava.dto.response;

import java.io.Serializable;

import lombok.Builder;
import lombok.Data;

/**
 * 
 * @author matia
 * Dto to send followStatus to the client.
 */
@Data
@Builder
public class ResFollowStatus implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private String followStatus;

}
