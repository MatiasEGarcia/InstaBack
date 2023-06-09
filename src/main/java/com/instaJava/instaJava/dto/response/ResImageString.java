package com.instaJava.instaJava.dto.response;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * @author matia
 * Dto to return image as String base 64 to the client.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResImageString implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private String image64;
}
