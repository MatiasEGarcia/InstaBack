package com.instaJava.instaJava.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import com.instaJava.instaJava.dto.FollowDto;
import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.dto.response.ResFollowStatus;
import com.instaJava.instaJava.dto.response.ResPaginationG;
import com.instaJava.instaJava.entity.Follow;

@Mapper(componentModel = "spring")
public interface FollowMapper {
	
	@Mapping(target = "followStatus", source= "followStatus" )
	ResFollowStatus followToResFollowStatus(Follow follow);
	
	FollowDto followToFollowDto(Follow follow);
	
	Follow followDtoToFollow(FollowDto followDto);
	
	List<FollowDto> followListToFollowDtoList(List<Follow> followList); 
	
	@Mapping(target ="list" , source = "page.content")
	@Mapping(target ="pageInfoDto.pageNo", source = "pageInfoDto.pageNo")
	@Mapping(target ="pageInfoDto.pageSize", source = "pageInfoDto.pageSize") 
	@Mapping(target ="pageInfoDto.sortField", source = "pageInfoDto.sortField") 
	@Mapping(target ="pageInfoDto.sortDir", source = "pageInfoDto.sortDir") 
	@Mapping(target ="pageInfoDto.totalPages" , source = "page.totalPages")
	@Mapping(target ="pageInfoDto.totalElements" , source = "page.totalElements")
	ResPaginationG<FollowDto> pageAndPageInfoDtoToResPaginationG(Page<Follow> page, PageInfoDto pageInfoDto);
	
	
}
