package com.instaJava.instaJava.service;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.instaJava.instaJava.dao.FollowerDao;
import com.instaJava.instaJava.entity.Follower;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.enums.FollowStatus;
import com.instaJava.instaJava.util.MessagesUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FollowerServiceImpl implements FollowerService{
	
	private final UserService userService;
	private final FollowerDao followerDao;
	private final MessagesUtils messUtils;
	
	@Override
	@Transactional
	public Follower save(Long FollowedId) {
		if(FollowedId == null) {
			throw new IllegalArgumentException(messUtils.getMessage("exepcion.argument.not.null"));
		}
		Follower follower = Follower.builder().build();
		User userFollowed = userService.findById(FollowedId); //if not exist throw exception
		User userFollower = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		follower.setUserFollowed(userFollowed);
		follower.setUserFollower((userService.findById(userFollower.getUserId())));//persistence context?If I don't do this user is detached
		if(userFollowed.isVisible()) {
			follower.setFollowStatus(FollowStatus.ACCEPTED);
		}else {
			follower.setFollowStatus(FollowStatus.IN_PROCESS);
		}
		return followerDao.save(follower);
	}

}
