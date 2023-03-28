package com.instaJava.instaJava.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.instaJava.instaJava.dto.response.ResFollowStatus;
import com.instaJava.instaJava.entity.Follower;

@Mapper(componentModel = "spring")
public interface FollowerMapper {
	
	@Mapping(target = "followStatus", source= "followStatus" )
	ResFollowStatus FollowerToResFollowStatus(Follower follower);
}
