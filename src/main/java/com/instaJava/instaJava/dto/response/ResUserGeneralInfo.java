package com.instaJava.instaJava.dto.response;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResUserGeneralInfo implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private ResUser user;
	
	private ResSocialInfo social;
}
