package com.instaJava.instaJava.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.instaJava.instaJava.dto.response.ResPublicatedImage;
import com.instaJava.instaJava.entity.PublicatedImage;
import com.instaJava.instaJava.entity.User;



@Mapper(componentModel = "spring")
public interface PublicatedImageMapper {
	
	@Mapping(target="id", source="publicatedImage.pubImaId")
	@Mapping(target="image" , source = "publicatedImage.image")
	@Mapping(target="userOwner", source="user.username")
	ResPublicatedImage publicatedImageAndUserToResPublicatedImage(PublicatedImage publicatedImage,User user);
}
