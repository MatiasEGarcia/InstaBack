package com.instaJava.instaJava.service;

import org.springframework.data.domain.Page;

import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.dto.request.ReqSearchList;
import com.instaJava.instaJava.entity.Follower;
import com.instaJava.instaJava.enums.FollowStatus;

public interface FollowerService {

	Follower save(Long FollowedId);
	
	Page<Follower> search(PageInfoDto pageInfoDto,ReqSearchList reqSearchList);
	
	Follower updateFollowStatusById(Long id, FollowStatus newStatus);
	
	Follower findById(Long id);
	
	void deleteById(Long id);
}
