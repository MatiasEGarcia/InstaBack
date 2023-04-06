package com.instaJava.instaJava.dto.response;

import java.io.Serializable;

import com.instaJava.instaJava.enums.FollowStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ResFollower implements Serializable{
	private static final long serialVersionUID = 1L;
	private Long followerId;
	private FollowStatus followStatus;
	private ResUser userFollowed;
	private ResUser userFollower;
}
