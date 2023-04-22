package com.instaJava.instaJava.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.dto.response.ResFollow;
import com.instaJava.instaJava.dto.response.ResFollowStatus;
import com.instaJava.instaJava.dto.response.ResPaginationG;
import com.instaJava.instaJava.entity.Follow;

@Mapper(componentModel = "spring")
public interface FollowMapper {
	
	@Mapping(target = "followStatus", source= "followStatus" )
	ResFollowStatus followToResFollowStatus(Follow follow);
	
	ResFollow followToResFollow(Follow follow);
	
	List<ResFollow> followListToResFollowList(List<Follow> followList); 
	
	@Mapping(target ="list" , source = "page.content")
	@Mapping(target ="pageInfoDto", source = "pageInfoDto") 
	@Mapping(target ="pageInfoDto.totalPages" , source = "page.totalPages")
	@Mapping(target ="pageInfoDto.totalElements" , source = "page.totalElements")
	ResPaginationG<ResFollow> pageAndPageInfoDtoToResPaginationG(Page<Follow> page, PageInfoDto pageInfoDto);
	
	
}
