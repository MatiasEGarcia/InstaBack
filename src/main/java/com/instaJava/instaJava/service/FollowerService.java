package com.instaJava.instaJava.service;

import org.springframework.data.domain.Page;

import com.instaJava.instaJava.dto.request.ReqSearch;
import com.instaJava.instaJava.entity.Follower;

public interface FollowerService {

	Follower save(Long FollowedId);
	
	Page<Follower> search(int pageNo, int pageSize, String sortField, String sortDir,ReqSearch reqSearch);
}
