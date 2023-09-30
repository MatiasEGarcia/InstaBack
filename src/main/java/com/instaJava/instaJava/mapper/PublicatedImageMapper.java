package com.instaJava.instaJava.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.dto.response.ResPaginationG;
import com.instaJava.instaJava.dto.response.ResPublicatedImage;
import com.instaJava.instaJava.entity.PublicatedImage;
import com.instaJava.instaJava.entity.User;



@Mapper(componentModel = "spring")
public interface PublicatedImageMapper {
	
	@Mapping(target="id", source="publicatedImage.pubImaId")
	@Mapping(target="image" , source = "publicatedImage.image")
	@Mapping(target="userOwner", source="user")
	ResPublicatedImage publicatedImageAndUserToResPublicatedImage(PublicatedImage publicatedImage,User user);
	
	
	@Mapping(target="id", source="publicatedImage.pubImaId")
	@Mapping(target="image" , source = "publicatedImage.image")
	@Mapping(target="userOwner", source="publicatedImage.userOwner")
	ResPublicatedImage publicatedImageToResPublicatedImage(PublicatedImage publicatedImage);
	
	List<ResPublicatedImage> listPublicatedImageToListResPublicatedImage(List<PublicatedImage> publicatedImages);

	@Mapping(target ="list" , source = "page.content")
	@Mapping(target ="pageInfoDto.pageNo", source = "pageInfoDto.pageNo")
	@Mapping(target ="pageInfoDto.pageSize", source = "pageInfoDto.pageSize") 
	@Mapping(target ="pageInfoDto.sortField", source = "pageInfoDto.sortField") 
	@Mapping(target ="pageInfoDto.sortDir", source = "pageInfoDto.sortDir") 
	@Mapping(target ="pageInfoDto.totalPages" , source = "page.totalPages")
	@Mapping(target ="pageInfoDto.totalElements" , source = "page.totalElements")
	ResPaginationG<ResPublicatedImage> pageAndPageInfoDtoToResPaginationG(Page<PublicatedImage> page,PageInfoDto pageInfoDto);
}
