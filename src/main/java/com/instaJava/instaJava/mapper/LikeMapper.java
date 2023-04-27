package com.instaJava.instaJava.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.instaJava.instaJava.dto.response.ResLike;
import com.instaJava.instaJava.dto.response.ResListG;
import com.instaJava.instaJava.entity.Like;

@Mapper(componentModel = "spring")
public interface LikeMapper {

	ResLike likeToResLike(Like like);
	
	// Dummy property to prevent Mapstruct complaining "Can't generate mapping method from iterable type to non-iterable type."
	@Mapping(target ="list" , source = "likeList")
	ResListG<ResLike> likeListToResListG(Integer dummy,List<Like> likeList);
	
}
