package com.instaback.dto.response;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * @author matia
 *	Dto to return error data to the client.
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RespValidError implements Serializable {

private static final long serialVersionUID = 1L;
	
	private String field;
	
	private String errorMessage;
}
