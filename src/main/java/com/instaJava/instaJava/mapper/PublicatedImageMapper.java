package com.instaJava.instaJava.mapper;

import java.util.List;
import java.util.Map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import com.instaJava.instaJava.dto.response.ResPaginationG;
import com.instaJava.instaJava.dto.response.ResPublicatedImage;
import com.instaJava.instaJava.entity.PublicatedImage;
import com.instaJava.instaJava.entity.User;



@Mapper(componentModel = "spring")
public interface PublicatedImageMapper {
	
	@Mapping(target="id", source="publicatedImage.pubImaId")
	@Mapping(target="image" , source = "publicatedImage.image")
	@Mapping(target="userOwner", source="user.username")
	ResPublicatedImage publicatedImageAndUserToResPublicatedImage(PublicatedImage publicatedImage,User user);
	
	
	@Mapping(target="id", source="publicatedImage.pubImaId")
	@Mapping(target="image" , source = "publicatedImage.image")
	@Mapping(target="userOwner", source="publicatedImage.userOwner.username")
	ResPublicatedImage publicatedImageToResPublicatedImage(PublicatedImage publicatedImage);
	
	List<ResPublicatedImage> listPublicatedImageToListResPublicatedImage(List<PublicatedImage> publicatedImages);
	
	@Mapping(target ="list" , source = "page.content")
	@Mapping(target ="totalPages" , source = "page.totalPages")
	@Mapping(target ="totalElements" , source = "page.totalElements")
	@Mapping(target ="actualPage" , source = "map.actualPage")
	@Mapping(target ="pageSize" , source = "map.pageSize")
	@Mapping(target ="sortField" , source = "map.sortField")
	@Mapping(target ="sortDir" , source = "map.sortDir")
	ResPaginationG<ResPublicatedImage> pageAndMapToResPaginationG(Page<PublicatedImage> page, Map<String,String> map);
	
	
}
