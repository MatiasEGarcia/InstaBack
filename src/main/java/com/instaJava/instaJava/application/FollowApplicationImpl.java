package com.instaJava.instaJava.application;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import com.instaJava.instaJava.dto.FollowDto;
import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.dto.request.ReqSearchList;
import com.instaJava.instaJava.dto.response.ResPaginationG;
import com.instaJava.instaJava.entity.Follow;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.enums.FollowStatus;
import com.instaJava.instaJava.mapper.FollowMapper;
import com.instaJava.instaJava.service.FollowService;
import com.instaJava.instaJava.service.NotificationService;
import com.instaJava.instaJava.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FollowApplicationImpl implements FollowApplication{

	private final UserService userService;
	private final FollowService fService;
	private final NotificationService notiService;
	private final FollowMapper fMapper;
	
	@Override
	public FollowDto save(Long followedId) {
		Follow followSaved;
		String customMessage;
		//followed user exists?
		User userFollowed = userService.findById(followedId);
		
		followSaved = fService.save(userFollowed);
		if (userFollowed.isVisible()) {
			customMessage = "A new user is following you";
		} else {
			customMessage = "A new user wants to follow you";
		}
		// save notification for the followed user
		notiService.saveNotificationOfFollow(followSaved, customMessage);
		return fMapper.followToFollowDto(followSaved);
	}

	@Override
	public ResPaginationG<FollowDto> search(int pageNo, int pageSize, String sortField, Direction sortDir, ReqSearchList reqSearchList) {
		Page<Follow> page;
		PageInfoDto pageInfoDto = new PageInfoDto(pageNo, pageSize,0,0 , sortField, sortDir);
		page = fService.search(pageInfoDto, reqSearchList);
		return fMapper.pageAndPageInfoDtoToResPaginationG(page, pageInfoDto);
	}

	@Override
	public FollowDto updateFollowStatusById(Long id, FollowStatus newStatus) {
		Follow followUpdated = fService.updateFollowStatusById(id, newStatus);
		return fMapper.followToFollowDto(followUpdated);
	}

	@Override
	public FollowDto deleteById(Long id) {
		Follow followDeleted = fService.deleteById(id);
		return fMapper.followToFollowDto(followDeleted);
	}

	@Override
	public FollowDto deleteByFollwedId(Long followedId) {
		Follow followDeleted = fService.deleteByFollwedId(followedId);
		return fMapper.followToFollowDto(followDeleted);
	}

	@Override
	public FollowDto updateFollowStatusByFollowerId(Long followerUserId, FollowStatus newStatus) {
		Follow followUpdated = fService.updateFollowStatusByFollowerId(followerUserId, newStatus);
		return fMapper.followToFollowDto(followUpdated);
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
