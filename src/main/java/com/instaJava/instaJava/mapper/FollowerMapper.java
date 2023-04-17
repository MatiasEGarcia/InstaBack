package com.instaJava.instaJava.mapper;

import java.util.List;
import java.util.Map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.dto.response.ResFollowStatus;
import com.instaJava.instaJava.dto.response.ResFollower;
import com.instaJava.instaJava.dto.response.ResPaginationG;
import com.instaJava.instaJava.entity.Follower;

@Mapper(componentModel = "spring")
public interface FollowerMapper {
	
	@Mapping(target = "followStatus", source= "followStatus" )
	ResFollowStatus FollowerToResFollowStatus(Follower follower);
	
	ResFollower followerToResFollower(Follower follower);
	
	List<ResFollower> followerListToResFollowerList(List<Follower> follower); 
	
	@Mapping(target ="list" , source = "page.content")
	@Mapping(target ="pageInfoDto", source = "pageInfoDto") 
	@Mapping(target ="pageInfoDto.totalPages" , source = "page.totalPages")
	@Mapping(target ="pageInfoDto.totalElements" , source = "page.totalElements")
	ResPaginationG<ResFollower> pageAndPageInfoDtoToResPaginationG(Page<Follower> page, PageInfoDto pageInfoDto);
	
	
}
