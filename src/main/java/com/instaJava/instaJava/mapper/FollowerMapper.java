package com.instaJava.instaJava.mapper;

import java.util.List;
import java.util.Map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

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
	@Mapping(target ="totalPages" , source = "page.totalPages")
	@Mapping(target ="totalElements" , source = "page.totalElements")
	@Mapping(target ="actualPage" , source = "map.actualPage")
	@Mapping(target ="pageSize" , source = "map.pageSize")
	@Mapping(target ="sortField" , source = "map.sortField")
	@Mapping(target ="sortDir" , source = "map.sortDir")
	ResPaginationG<ResFollower> pageAndMapToResPaginationG(Page<Follower> page, Map<String,String> map);
	
	
}
