package com.instaJava.instaJava.dto.response;

import java.io.Serializable;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

/**
 * Generic Dto to error messages sended to the client.
 * @author matia
 *
 */
@Data
@Builder
public class ResErrorMessage implements Serializable {

	private static final long serialVersionUID = 1L;

	private String error;
	private String message;
	private Map<String,String> details;
}
