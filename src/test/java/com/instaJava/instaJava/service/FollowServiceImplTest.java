package com.instaJava.instaJava.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.instaJava.instaJava.dao.FollowDao;
import com.instaJava.instaJava.dto.FollowDto;
import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.dto.UserDto;
import com.instaJava.instaJava.dto.request.ReqSearch;
import com.instaJava.instaJava.dto.request.ReqSearchList;
import com.instaJava.instaJava.dto.response.ResPaginationG;
import com.instaJava.instaJava.entity.Follow;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.enums.FollowStatus;
import com.instaJava.instaJava.enums.GlobalOperationEnum;
import com.instaJava.instaJava.exception.AlreadyExistsException;
import com.instaJava.instaJava.exception.InvalidActionException;
import com.instaJava.instaJava.exception.RecordNotFoundException;
import com.instaJava.instaJava.mapper.FollowMapper;
import com.instaJava.instaJava.util.MessagesUtils;
import com.instaJava.instaJava.util.PageableUtils;

@ExtendWith(MockitoExtension.class)
class FollowServiceImplTest {

	@Mock
	private Authentication auth;
	@Mock
	private SecurityContext securityContext;
	@Mock
	private UserService userService;
	@Mock
	private NotificationService notificationService;
	@Mock
	private FollowDao followDao;
	@Mock
	private SpecificationService<Follow> specService;
	@Mock
	private FollowMapper followMapper;
	@Mock
	private MessagesUtils messUtils;
	@Mock
	private PageableUtils pageUtils;
	@InjectMocks
	private FollowServiceImpl followService;

	// save
	@Test
	void saveFollowedIdNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> followService.save(null));
		verify(notificationService, never()).saveNotificationOfFollow(any(Follow.class), eq(""));
		verify(followDao, never()).save(any(Follow.class));
	}

	@Test
	void saveFollowedNoExistThrow() {
		Long followedId = 1L;
		when(userService.findById(followedId)).thenThrow(RecordNotFoundException.class);
		assertThrows(RecordNotFoundException.class, () -> followService.save(followedId));
		verify(notificationService, never()).saveNotificationOfFollow(any(Follow.class), eq(""));
		verify(followDao, never()).save(any(Follow.class));
	}

	@Test
	void saveFollowRecordAlreadyExistsThrow() {
		User userFollower = User.builder() // who is authenticated and wants to create follow record.
				.id(2L).build();
		User userFollowed = User.builder().id(1L).visible(true).build();

		FollowServiceImpl followServiceSpy = spy(followService);

		when(userService.findById(userFollowed.getId())).thenReturn(userFollowed);
		// setting authenticated user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(userFollower);

		doReturn(true).when(followServiceSpy).existsByFollowedAndFollower(userFollowed.getId());
		assertThrows(AlreadyExistsException.class, () -> followServiceSpy.save(userFollowed.getId()));

		verify(userService).findById(userFollowed.getId());
		verify(notificationService, never()).saveNotificationOfFollow(any(Follow.class), eq(""));
		verify(followDao, never()).save(any(Follow.class));
	}

	@Test
	void saveFollowReturnsNotNull() {
		// who is authenticated and wants to create follow record.
		User userFollower = User.builder().id(2L).visible(true).build();
		User userFollowed = User.builder().id(1L).visible(true).build();
		// follow saved and returned by followDao.
		Follow follow = Follow.builder().follower(userFollower).followed(userFollowed)
				.followStatus(FollowStatus.ACCEPTED).build();
		FollowDto followDto = new FollowDto();
		FollowServiceImpl followServiceSpy = spy(followService);

		when(userService.findById(userFollowed.getId())).thenReturn(userFollowed);
		// setting authenticated user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(userFollower);
		doReturn(false).when(followServiceSpy).existsByFollowedAndFollower(userFollowed.getId());
		when(followDao.save(any(Follow.class))).thenReturn(follow);
		when(followMapper.followToFollowDto(follow)).thenReturn(followDto);

		assertNotNull(followServiceSpy.save(userFollowed.getId()));

		verify(userService).findById(userFollowed.getId());
		verify(notificationService).saveNotificationOfFollow(any(Follow.class), eq("A new user is following you"));
		verify(followDao).save(any(Follow.class));
	}

	// search
	@Test
	void searchReqSearchListArgNullTrow() {
		assertThrows(IllegalArgumentException.class, () -> followService.search(any(PageInfoDto.class), null));
	}

	@Test
	void searchPageInfoDtoArgNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> followService.search(null, any(ReqSearchList.class)));
	}

	@Test
	void searchPageInfoDtoSortFieldNullThrow() {
		PageInfoDto pageInfoDto = PageInfoDto.builder().pageNo(1).pageSize(10).sortDir(Direction.ASC).build();
		assertThrows(IllegalArgumentException.class, () -> followService.search(pageInfoDto, any(ReqSearchList.class)));
	}

	@Test
	void searchPageInfoDtoSortDirNullThrow() {
		PageInfoDto pageInfoDto = PageInfoDto.builder().pageNo(1).pageSize(10).sortField("random").build();
		assertThrows(IllegalArgumentException.class, () -> followService.search(pageInfoDto, any(ReqSearchList.class)));
	}

	@Test
	void searchFollowsRecordsNotFoundThrow() {
		PageInfoDto pageInfoDto = PageInfoDto.builder().pageNo(0).pageSize(10).sortDir(Direction.ASC)
				.sortField("username").build();
		ReqSearch reqSearch = ReqSearch.builder().build();
		ReqSearchList rqSearchList = ReqSearchList.builder().reqSearchs(List.of(reqSearch))
				.globalOperator(GlobalOperationEnum.AND).build();

		// spec for example only, does not match reqSearch
		Specification<Follow> spec = (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("followId"), 1);
		when(specService.getSpecification(rqSearchList.getReqSearchs(), GlobalOperationEnum.AND)).thenReturn(spec);
		when(pageUtils.getPageable(pageInfoDto)).thenReturn(Pageable.unpaged());
		when(followDao.findAll(eq(spec), any(Pageable.class))).thenReturn(Page.empty());
		
		assertThrows(RecordNotFoundException.class, () -> followService.search(pageInfoDto, rqSearchList));
	
		verify(followDao).findAll(eq(spec), any(Pageable.class));
	}
	
	@Test
	void searchReturnsNotNull() {
		PageInfoDto pageInfoDto = PageInfoDto.builder().pageNo(0).pageSize(10).sortDir(Direction.ASC)
				.sortField("username").build();
		ReqSearch reqSearch = ReqSearch.builder().build();
		ReqSearchList rqSearchList = ReqSearchList.builder().reqSearchs(List.of(reqSearch))
				.globalOperator(GlobalOperationEnum.AND).build();
		ResPaginationG<FollowDto> resPag= new ResPaginationG<FollowDto>();
		Page<Follow> page = new PageImpl<>(List.of(new Follow()));

		// spec for example only, does not match reqSearch
		Specification<Follow> spec = (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("followId"), 1);
		when(specService.getSpecification(rqSearchList.getReqSearchs(), GlobalOperationEnum.AND)).thenReturn(spec);
		when(pageUtils.getPageable(pageInfoDto)).thenReturn(Pageable.unpaged());
		when(followDao.findAll(eq(spec), any(Pageable.class))).thenReturn(page);
		when(followMapper.pageAndPageInfoDtoToResPaginationG(page, pageInfoDto)).thenReturn(resPag);
		
		
		assertNotNull(followService.search(pageInfoDto, rqSearchList));
		
		verify(followDao).findAll(eq(spec), any(Pageable.class));
	}
	
	

	// updateFollowStatusById
	@Test
	void updateFollowStatusByIdParamIdNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> followService.updateFollowStatusById(null, FollowStatus.ACCEPTED));
	}
	@Test
	void updateFollowStatusByIdParamFollowStatusNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> followService.updateFollowStatusById(1L, null));
	}
	
	@Test
	void updateFollowStatusByIdAuhtUserAndFollowedInFollowRecordNotSameThrow() {
		User authUser = User.builder().id(2L).build();
		UserDto otherUserFollowed = UserDto.builder().id("3").build();
		FollowDto followDto = FollowDto.builder().followed(otherUserFollowed).followStatus(FollowStatus.ACCEPTED).build();
		FollowServiceImpl spyFollowService = spy(followService);
		
		
		doReturn(followDto).when(spyFollowService).findById(anyLong());
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(authUser);
		
		assertThrows(IllegalArgumentException.class, () -> spyFollowService.updateFollowStatusById(1L, FollowStatus.REJECTED));
		
		verify(followDao,never()).save(any(Follow.class));
	}

	@Test
	void updateFollowStatusByIdReturnNotNull() {
		User userFollowedWhoAuth = User.builder().id(2L).build();
		UserDto userAuthFollowedDto = UserDto.builder().id("2").build();
		FollowDto followDto = FollowDto.builder().followed(userAuthFollowedDto).followStatus(FollowStatus.ACCEPTED).build();
		FollowServiceImpl spyFollowService = spy(followService);
		Follow followToSave = new Follow();
		
		doReturn(followDto).when(spyFollowService).findById(anyLong());
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(userFollowedWhoAuth);
		when(followMapper.followDtoToFollow(followDto)).thenReturn(followToSave);
		when(followDao.save(followToSave)).thenReturn(followToSave);
		when(followMapper.followToFollowDto(followToSave)).thenReturn(followDto);

		
		assertNotNull(spyFollowService.updateFollowStatusById(2L, FollowStatus.ACCEPTED));
		
		verify(followDao).save(followToSave);
	}

	//getFollowStatusByFollowerId
	@Test
	void updateFollowStatusByFollowerParamFollowerIdNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> followService.updateFollowStatusByFollower(null, FollowStatus.ACCEPTED));
	}
	
	@Test
	void updateFollowStatusByFollowerParamFollowStatusNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> followService.updateFollowStatusByFollower(1L, null));
	}
	
	@Test
	void updateFollowStatusByFollowerFollowRecordNotFoundThrow() {
		Long followerId = 1L;
		User userFollowedWhoAuth = User.builder().id(2L).build();
		//auth user.
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(userFollowedWhoAuth);
		
		when(followDao.findOneByFollowedIdAndFollowerId(userFollowedWhoAuth.getId(), followerId)).thenReturn(Optional.empty());
		
		assertThrows(RecordNotFoundException.class, () -> followService.updateFollowStatusByFollower(followerId, FollowStatus.ACCEPTED));
		
		verify(followMapper,never()).followToFollowDto(any(Follow.class));
	}
	
	@Test
	void updateFollowStatusByFollowerReturnsNotNull() {
		Long followerId = 1L;
		FollowStatus newFollowStatus = FollowStatus.ACCEPTED;
		User userFollowedWhoAuth = User.builder().id(2L).build();
		Follow follow = new Follow();
		FollowDto followDto = FollowDto.builder().followStatus(newFollowStatus).build();
		
		//auth user.
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(userFollowedWhoAuth);
		//dao
		when(followDao.findOneByFollowedIdAndFollowerId(userFollowedWhoAuth.getId(), followerId)).thenReturn(Optional.of(follow));
		//setting new follow statuts
		follow.setFollowStatus(newFollowStatus);
		when(followDao.save(any(Follow.class))).thenReturn(follow);
		//mapper
		when(followMapper.followToFollowDto(any(Follow.class))).thenReturn(followDto);
		
		assertNotNull(followService.updateFollowStatusByFollower(followerId, FollowStatus.ACCEPTED));
	}
	
	
	//findById
	@Test
	void findByIdParamIdNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> followService.findById(null));
	}
	
	@Test
	void findByIdNoExistThrow() {
		when(followDao.findById(1L)).thenReturn(Optional.empty());
		assertThrows(RecordNotFoundException.class, () ->followService.findById(1L));
		verify(followDao).findById(anyLong());
	}

	@Test
	void findByIdExistReturnNotNull() {
		Follow follow = new Follow();
		FollowDto followDto = new FollowDto();
		when(followDao.findById(1L)).thenReturn(Optional.of(follow));
		when(followMapper.followToFollowDto(follow)).thenReturn(followDto);
		assertNotNull(followService.findById(1L));
		verify(followDao).findById(anyLong());
	}

	
	//existsByFollowedAndFollower
	@Test
	void existsByFollowedAndFollowerFollowedIdNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> followService.existsByFollowedAndFollower(null));
	}

	@Test
	void existsByFollowedAndFollowerRecordNoExistReturnsFalse() {
		User userWhoAuth = User.builder().id(2L).visible(false).build();
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(userWhoAuth);
		when(followDao.existsByFollowedIdAndFollowerId(anyLong(), eq(userWhoAuth.getId()))).thenReturn(false);

		assertFalse(followService.existsByFollowedAndFollower(1L));
	}

	@Test
	void existsByFollowedAndFollowerRecordExistReturnsTrue() {
		User userWhoAuth = User.builder().id(2L).visible(false).build();
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(userWhoAuth);
		when(followDao.existsByFollowedIdAndFollowerId(anyLong(), eq(userWhoAuth.getId()))).thenReturn(true);

		assertTrue(followService.existsByFollowedAndFollower(1L));
	}

	//delete
	@Test
	void deleteByIdThrow() {
		assertThrows(IllegalArgumentException.class, () -> followService.deleteById(null));
	}

	@Test
	void deleteByIdFollowerNotSameThrow() {
		User userWhoAuth = User.builder().id(2L).visible(false).build();
		UserDto followDtoOwn = UserDto.builder().id("3").visible(false).build();
		FollowDto followDto = FollowDto.builder().follower(followDtoOwn).build(); // I will only compare follower owner with the
																		// auth user.
		FollowServiceImpl follSerSpy = spy(followService);
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(userWhoAuth);
		doReturn(followDto).when(follSerSpy).findById(anyLong());
		assertThrows(InvalidActionException.class, () -> follSerSpy.deleteById(anyLong()),
				"if followOwner is not the same than" + " the authenticated user , the follow cannot be deleted");
	}

	@Test
	void deleteById() {
		//same userId, same user follower
		User userWhoAuth = User.builder().id(2L).visible(false).build();
		UserDto followDtoOwn = UserDto.builder().id("2").visible(false).build();
		Follow followToDelete = Follow.builder().follower(userWhoAuth).build();
		FollowDto followDto = FollowDto.builder().follower(followDtoOwn).build(); 
		
		
		FollowServiceImpl follSerSpy = spy(followService);
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(userWhoAuth);
		doReturn(followDto).when(follSerSpy).findById(anyLong());
		when(followMapper.followDtoToFollow(followDto)).thenReturn(followToDelete);
		
		follSerSpy.deleteById(anyLong());
		
		verify(followDao).delete(followToDelete);
	}

	//getFollowStatusByFollowedId
	@Test
	void getFollowStatusByFollowedIdIdNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> followService.getFollowStatusByFollowedId(null));
	}
	
	@Test
	void getFollowStatusByFollowedIdUserNoVisibleFollowedRecordNoExistReturnNotAsked() {
		User userWhoAuth = User.builder().id(2L).visible(false).build();
		UserDto userDtoFollwed = UserDto.builder().id("1").visible(false).build();
		Long followedId = 1L;
		
		when(userService.getById(1L)).thenReturn(userDtoFollwed);
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(userWhoAuth);
		when(followDao.findOneByFollowedIdAndFollowerId(followedId, userWhoAuth.getId())).thenReturn(Optional.empty());
		
		FollowStatus followStatus = followService.getFollowStatusByFollowedId(1L);
		
		if (!followStatus.equals(FollowStatus.NOT_ASKED))
			fail("if the follower record no exist should return followStatus NOT_ASKED");
	}
	@Test
	void getFollowStatusByFollowedIdUserNoVisibleFollowedRecordExistReturnNotNull() {
		User userWhoAuth = User.builder().id(2L).visible(false).build();
		UserDto userDtoFollwed = UserDto.builder().id("1").visible(false).build();
		Follow follow = Follow.builder().followStatus(FollowStatus.IN_PROCESS).build();
		Long followedId = 1L;
		
		when(userService.getById(1L)).thenReturn(userDtoFollwed);
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(userWhoAuth);
		when(followDao.findOneByFollowedIdAndFollowerId(followedId, userWhoAuth.getId())).thenReturn(Optional.of(follow));
		
		assertNotNull(followService.getFollowStatusByFollowedId(1L));
		
	}
	
	//getFollowStatusByFollowerId
	@Test
	void getFollowStatusByFollowerIdParamIdNullThrow() {
		assertThrows(IllegalArgumentException.class , () -> followService.getFollowStatusByFollowerId(null));
	}

	@Test
	void getFollowStatusByFollowerIdReturnsNotNull() {
		User userWhoAuth = User.builder().id(2L).visible(false).build();
		UserDto userDtoFollwer = UserDto.builder().id("1").visible(false).build();
		Follow follow = Follow.builder().followStatus(FollowStatus.IN_PROCESS).build();
		Long followerId = 1L;
		
		//user service
		when(userService.getById(followerId)).thenReturn(userDtoFollwer);
		//auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(userWhoAuth);
		//follow dao
		when(followDao.findOneByFollowedIdAndFollowerId(userWhoAuth.getId(), followerId))
			.thenReturn(Optional.of(follow));
		
		assertNotNull(followService.getFollowStatusByFollowerId(followerId));
		
	}

	//countByFollowStatusAndFollowed
	@Test
	void countByFollowStatusAndFollowedParamFollowStatusNullThrow() {
		Long followedId = 1L;
		assertThrows(IllegalArgumentException.class, () -> followService.countByFollowStatusAndFollowed(null, followedId));
		verify(followDao, never()).countByFollowedIdAndFollowStatus(followedId, null);
	}
	@Test
	void countByFollowStatusAndFollowedParamFollowedIdNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> followService.countByFollowStatusAndFollowed(FollowStatus.ACCEPTED, null));
		verify(followDao, never()).countByFollowedIdAndFollowStatus(null, FollowStatus.ACCEPTED);
	}
	@Test
	void countByFollowStatusAndFollowedReturnsNotNull() {
		Long followedId = 1L;
		FollowStatus followStatus = FollowStatus.ACCEPTED;
		Long count = 2L;
		when(followDao.countByFollowedIdAndFollowStatus(followedId, followStatus)).thenReturn(count);
		
		assertNotNull(followService.countByFollowStatusAndFollowed(followStatus, followedId));
	}
	
	
	//countByFollowStatusAndFollower
	@Test 
	void countByFollowStatusAndFollowerParamFollowStatusNullThrow() {
		Long followerId = 1L;
		assertThrows(IllegalArgumentException.class, () -> followService.countByFollowStatusAndFollower(null, followerId));
		verify(followDao, never()).countByFollowedIdAndFollowStatus(followerId, null);
	}
	@Test
	void countByFollowStatusAndFollowerParamFollowedIdNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> followService.countByFollowStatusAndFollower(FollowStatus.ACCEPTED, null));
		verify(followDao, never()).countByFollowedIdAndFollowStatus(null, FollowStatus.ACCEPTED);
	}
	@Test
	void countByFollowStatusAndFollowerReturnsNotNull() {
		Long followerId = 1L;
		FollowStatus followStatus = FollowStatus.ACCEPTED;
		Long count = 2L;
		when(followDao.countByFollowerIdAndFollowStatus(followerId, followStatus)).thenReturn(count);
		
		assertNotNull(followService.countByFollowStatusAndFollower(followStatus, followerId));
	}
	

}






























