package com.instaJava.instaJava.dto.response;

import java.io.Serializable;

import com.instaJava.instaJava.dto.UserDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserGeneralInfoDto implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private UserDto user;
	
	private SocialInfoDto social;
}
