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
public class ResFollow implements Serializable{
	private static final long serialVersionUID = 1L;
	private Long followId;
	private FollowStatus followStatus;
	private ResUser followed;
	private ResUser follower;
}
