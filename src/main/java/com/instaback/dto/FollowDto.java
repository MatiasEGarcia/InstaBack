package com.instaback.dto;

import java.io.Serializable;

import com.instaback.enums.FollowStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 
 * @author matia
 * Dto to send Follow data to the client.
 * With user information using Dto {@link com.instaback.dto.UserDto}.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class FollowDto implements Serializable{
	private static final long serialVersionUID = 1L;
	private String id;
	private FollowStatus followStatus;
	private UserDto followed;
	private UserDto follower;
}
