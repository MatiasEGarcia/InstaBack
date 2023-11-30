package com.instaJava.instaJava.mapper;

import org.mapstruct.Mapper;

import com.instaJava.instaJava.dto.response.LikeDto;
import com.instaJava.instaJava.entity.Like;

@Mapper(componentModel = "spring")
public interface LikeMapper {

	LikeDto likeToLikeDto(Like like);
}
