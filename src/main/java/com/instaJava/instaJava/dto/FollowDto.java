package com.instaJava.instaJava.dto;

import java.io.Serializable;

import com.instaJava.instaJava.dto.response.ResUser;
import com.instaJava.instaJava.enums.FollowStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 
 * @author matia
 * Dto to send Follow data to the client.
 * With user information using Dto {@link com.instaJava.instaJava.dto.response.ResUser}.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class FollowDto implements Serializable{
	private static final long serialVersionUID = 1L;
	private String followId;
	private FollowStatus followStatus;
	private ResUser followed;
	private ResUser follower;
}
