package com.instaJava.instaJava.service;

import org.springframework.data.domain.Page;

import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.dto.request.ReqSearchList;
import com.instaJava.instaJava.entity.Follow;
import com.instaJava.instaJava.enums.FollowStatus;

public interface FollowService {

	Follow save(Long FollowedId);
	
	Page<Follow> search(PageInfoDto pageInfoDto,ReqSearchList reqSearchList);
	
	Follow updateFollowStatusById(Long id, FollowStatus newStatus);
	
	Follow findById(Long id);
	
	void deleteById(Long id);
	
	FollowStatus getFollowStatusByFollowedId(Long followedId);
	
	Long countFollowedByUserId(Long id);
	
	Long countFollowerByUserId(Long id);
	
}