package com.instaback.mapper;

import org.mapstruct.Mapper;

import com.instaback.dto.response.LikeDto;
import com.instaback.entity.Like;

@Mapper(componentModel = "spring")
public interface LikeMapper {

	LikeDto likeToLikeDto(Like like);
}
