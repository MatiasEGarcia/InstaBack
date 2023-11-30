package com.instaJava.instaJava.dto.response;

import java.io.Serializable;

import com.instaJava.instaJava.enums.FollowStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SocialInfoDto implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private String numberPublications; // is a number, but I don't return longs, so I send it as String.
	
	private String numberFollowers; // is a number, but I don't return longs, so I send it as String.
	
	private String numberFollowed; // is a number, but I don't return longs, so I send it as String.
	
	private FollowStatus followStatus;//follow status between the auth user and the user searched.
}
