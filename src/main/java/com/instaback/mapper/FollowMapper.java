package com.instaback.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import com.instaback.dto.FollowDto;
import com.instaback.dto.PageInfoDto;
import com.instaback.dto.response.ResFollowStatus;
import com.instaback.dto.response.ResPaginationG;
import com.instaback.entity.Follow;

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
