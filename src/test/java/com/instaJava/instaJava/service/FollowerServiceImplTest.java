package com.instaJava.instaJava.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.instaJava.instaJava.dao.FollowerDao;
import com.instaJava.instaJava.entity.Follower;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.enums.FollowStatus;
import com.instaJava.instaJava.util.MessagesUtils;

@ExtendWith(MockitoExtension.class)
class FollowerServiceImplTest {

	@Mock private Authentication auth;
	@Mock private SecurityContext securityContext;
	@Mock private UserService userService;
	@Mock private FollowerDao followerDao;
	@Mock private MessagesUtils messUtils;
	@InjectMocks private FollowerServiceImpl followerService;
	
	@Test
	void saveFollowedIdNullThrow() {
		assertThrows(IllegalArgumentException.class,
				() -> followerService.save(null));
	}
	
	@Test
	void saveFollowedNoExistThrow() {
		Long followedId= 1L;
		when(userService.findById(followedId)).thenThrow(IllegalArgumentException.class);
		assertThrows(IllegalArgumentException.class,
				() -> followerService.save(followedId));
	}
	
	@Test
	void save() {
		User userFollower = User.builder()
				.userId(2L)
				.build();
		User userFollowed = User.builder()
				.userId(1L)
				.visible(true)
				.build();
		Follower follower = Follower.builder()
				.userFollowed(userFollowed)
				.userFollower(userFollower)
				.followStatus(FollowStatus.ACCEPTED)
				.build();
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
				.thenReturn(userFollower);
		when(userService.findById(userFollowed.getUserId())).thenReturn(userFollowed);
		when(userService.findById(userFollower.getUserId())).thenReturn(userFollower);
		when(followerDao.save(follower)).thenReturn(follower);
		assertNotNull(followerService.save(userFollowed.getUserId()));
		verify(followerDao).save(follower);
		
	}
	
	@Test
	void findByIdNoExistThrow() {
		when(followerDao.findById(1L)).thenReturn(Optional.empty());
		assertThrows(IllegalArgumentException.class, () ->followerService.findById(1L));
	}
	
	@Test
	void findByIdExistReturnNotNull() {
		when(followerDao.findById(1L)).thenReturn(Optional.of(new Follower()));
		assertNotNull(followerService.findById(1L));
	}

	@Test
	void updateFollowStatusByIdFollowedNotSameThrow() {
		User userFollowedWhoAuth= User.builder()
				.userId(2L)
				.build();
		User otherUserFollowed = User.builder()
				.userId(3L)
				.build();
		Follower follower = Follower.builder()
				.userFollowed(otherUserFollowed)
				.followStatus(FollowStatus.ACCEPTED)
				.build();
		when(followerDao.findById(2L)).thenReturn(Optional.of(follower));
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
				.thenReturn(userFollowedWhoAuth);
		assertThrows(IllegalArgumentException.class,() -> followerService.updateFollowStatusById(2L, FollowStatus.ACCEPTED));
		verify(followerDao).findById(2L);
		verify(followerDao,never()).save(follower);
	}

	
	@Test
	void updateFollowStatusByIdReturnNotNull() {
		User userFollowedWhoAuth= User.builder()
				.userId(2L)
				.build();
		Follower follower = Follower.builder()
				.userFollowed(userFollowedWhoAuth)
				.followStatus(FollowStatus.ACCEPTED)
				.build();
		when(followerDao.findById(2L)).thenReturn(Optional.of(follower));
		when(followerDao.save(follower)).thenReturn(follower);
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
				.thenReturn(userFollowedWhoAuth);
		assertNotNull(followerService.updateFollowStatusById(2L, FollowStatus.ACCEPTED));
		verify(followerDao).findById(2L);
		verify(followerDao).save(follower);
	}
	
	@Test
	void updateFollowStatusByIdArgFollowStatusNullThrow() {
		assertThrows(IllegalArgumentException.class,() -> followerService.updateFollowStatusById(1L, null));
	}
	
}
