package com.instaback.dto.response;

import java.io.Serializable;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic Dto to error messages sended to the client.
 * @author matia
 *
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ResErrorMessage implements Serializable {

	private static final long serialVersionUID = 1L;

	private String error;
	private String message;
	private Map<String,String> details;
}
