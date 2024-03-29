package com.instaback.dto.response;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * @author matia
 *	Dto to return a simple message to the client.
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResMessage implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String message;
}
