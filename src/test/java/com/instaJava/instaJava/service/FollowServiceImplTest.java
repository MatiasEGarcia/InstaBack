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
import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.dto.request.ReqSearch;
import com.instaJava.instaJava.dto.request.ReqSearchList;
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
	void saveFollowRecordAlreadyExistsThrow() {
		User userFollower = User.builder() // who is authenticated and wants to create follow record.
				.id(2L).build();
		User userFollowed = User.builder().id(1L).visible(true).build();

		FollowServiceImpl followServiceSpy = spy(followService);

		// setting authenticated user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(userFollower);

		doReturn(true).when(followServiceSpy).existsByFollowedAndFollower(userFollowed.getId());
		assertThrows(AlreadyExistsException.class, () -> followServiceSpy.save(userFollowed));

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
		FollowServiceImpl followServiceSpy = spy(followService);

		// setting authenticated user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(userFollower);
		doReturn(false).when(followServiceSpy).existsByFollowedAndFollower(userFollowed.getId());
		when(followDao.save(any(Follow.class))).thenReturn(follow);

		assertNotNull(followServiceSpy.save(userFollowed));
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
		Page<Follow> page = new PageImpl<>(List.of(new Follow()));

		// spec for example only, does not match reqSearch
		Specification<Follow> spec = (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("followId"), 1);
		when(specService.getSpecification(rqSearchList.getReqSearchs(), GlobalOperationEnum.AND)).thenReturn(spec);
		when(pageUtils.getPageable(pageInfoDto)).thenReturn(Pageable.unpaged());
		when(followDao.findAll(eq(spec), any(Pageable.class))).thenReturn(page);

		assertNotNull(followService.search(pageInfoDto, rqSearchList));

		verify(followDao).findAll(eq(spec), any(Pageable.class));
	}

	// updateFollowStatusById
	@Test
	void updateFollowStatusByIdParamIdNullThrow() {
		assertThrows(IllegalArgumentException.class,
				() -> followService.updateFollowStatusById(null, FollowStatus.ACCEPTED));
	}

	@Test
	void updateFollowStatusByIdParamFollowStatusNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> followService.updateFollowStatusById(1L, null));
	}

	@Test
	void updateFollowStatusByIdFollowNotFoundThrow() {
		Long id = 1L;
		when(followDao.findById(id)).thenReturn(Optional.empty());

		assertThrows(RecordNotFoundException.class,
				() -> followService.updateFollowStatusById(id, FollowStatus.REJECTED));

		verify(followDao, never()).save(any(Follow.class));
	}

	@Test
	void updateFollowStatusByIdAuhtUserAndFollowedInFollowRecordNotSameThrow() {
		Long id = 1L;
		User authUser = User.builder().id(2L).build();
		User otherUserFollowed = User.builder().id(3L).build();
		Follow followFound = Follow.builder().followed(otherUserFollowed).build();

		when(followDao.findById(id)).thenReturn(Optional.of(followFound));
		// auth
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(authUser);

		assertThrows(InvalidActionException.class,
				() -> followService.updateFollowStatusById(id, FollowStatus.REJECTED));

		verify(followDao, never()).save(any(Follow.class));
	}

	@Test
	void updateFollowStatusByIdReturnNotNull() {
		Long id = 2L;
		User userFollowedWhoAuth = User.builder().id(2L).build();
		Follow followFound = Follow.builder().followed(userFollowedWhoAuth).build();

		when(followDao.findById(id)).thenReturn(Optional.of(followFound));

		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(userFollowedWhoAuth);

		assertNotNull(followService.updateFollowStatusById(id, FollowStatus.ACCEPTED));
	}

	// updateFollowStatusByFollowerId

	@Test
	void updateFollowStatusByFollowerIdParamFollowerIdNullThrow() {
		assertThrows(IllegalArgumentException.class,
				() -> followService.updateFollowStatusByFollowerId(null, FollowStatus.ACCEPTED));
	}

	@Test
	void updateFollowStatusByFollowerIdParamFollowStatusNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> followService.updateFollowStatusByFollowerId(1L, null));
	}

	@Test
	void updateFollowStatusByFollowerIdFollowRecordNotFoundThrow() {
		Long followerId = 1L;
		User userFollowedWhoAuth = User.builder().id(2L).build();
		// auth user.
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(userFollowedWhoAuth);

		when(followDao.findOneByFollowedIdAndFollowerId(userFollowedWhoAuth.getId(), followerId))
				.thenReturn(Optional.empty());

		assertThrows(RecordNotFoundException.class,
				() -> followService.updateFollowStatusByFollowerId(followerId, FollowStatus.ACCEPTED));

		verify(followMapper, never()).followToFollowDto(any(Follow.class));
	}

	@Test
	void updateFollowStatusByFollowerIdReturnsNotNull() {
		Long followerId = 1L;
		User userFollowedWhoAuth = User.builder().id(2L).build();
		Follow follow = new Follow();

		// auth user.
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(userFollowedWhoAuth);
		// dao
		when(followDao.findOneByFollowedIdAndFollowerId(userFollowedWhoAuth.getId(), followerId))
				.thenReturn(Optional.of(follow)); // setting new follow statuts

		assertNotNull(followService.updateFollowStatusByFollowerId(followerId, FollowStatus.ACCEPTED));
	}

	// existsByFollowedAndFollower

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

	// deleteById

	@Test
	void deleteByIdThrow() {
		assertThrows(IllegalArgumentException.class, () -> followService.deleteById(null));
	}

	@Test
	void deleteByIdFollowNotFoundThrow() {
		Long id = 5L;
		when(followDao.findById(id)).thenReturn(Optional.empty());
		assertThrows(RecordNotFoundException.class, () -> followService.deleteById(id));
	}

	@Test
	void deleteByIdFollowerNotSameThrow() {
		Long id = 5L;
		User userWhoAuth = User.builder().id(2L).visible(false).build();
		User otherUser = User.builder().id(5L).build();
		Follow followFound = Follow.builder().follower(otherUser).build();

		when(followDao.findById(id)).thenReturn(Optional.of(followFound));
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(userWhoAuth);
		assertThrows(InvalidActionException.class, () -> followService.deleteById(id),
				"if followOwner is not the same than" + " the authenticated user , the follow cannot be deleted");
	}

	@Test
	void deleteById() { // same userId, same user follower
		Long id = 5L;
		User userWhoAuth = User.builder().id(2L).visible(false).build();
		Follow followFound = Follow.builder().follower(userWhoAuth).build();

		when(followDao.findById(id)).thenReturn(Optional.of(followFound));
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(userWhoAuth);

		assertNotNull(followService.deleteById(id));

		verify(followDao).delete(followFound);
	}

	// deleteByFollwedId

	@Test
	void deleteByFollwedIdParamFollowedIdNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> followService.deleteByFollwedId(null));
	}

	@Test
	void deleteByFollowedIdFollowRecordNotFoundThrow() {
		Long followedId = 1L;
		User userWhoAuth = User.builder().id(2L).visible(false).build(); // auth
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(userWhoAuth); // dao
		when(followDao.findOneByFollowedIdAndFollowerId(followedId, userWhoAuth.getId())).thenReturn(Optional.empty());
		assertThrows(RecordNotFoundException.class, () -> followService.deleteByFollwedId(followedId));
		verify(followDao, never()).deleteById(followedId);
	}

	@Test
	void deleteByFollowedIdReturnNotNull() {
		Long followedId = 1L;
		User userWhoAuth = User.builder().id(2L).visible(false).build(); // auth
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(userWhoAuth);
		Follow followFound = new Follow(followedId);
		when(followDao.findOneByFollowedIdAndFollowerId(followedId, userWhoAuth.getId()))
				.thenReturn(Optional.of(followFound));
		assertNotNull(followService.deleteByFollwedId(followedId));
		verify(followDao).deleteById(followedId);
	}

	// getFollowStatusByFollowedId

	@Test
	void getFollowStatusByFollowedIdIdNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> followService.getFollowStatusByFollowedId(null));
	}

	@Test
	void getFollowStatusByFollowedIdUserNoVisibleFollowedRecordNoExistReturnNotAsked() {
		User userWhoAuth = User.builder().id(2L).visible(false).build();
		Long followedId = 1L;

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
		Follow follow = Follow.builder().followStatus(FollowStatus.IN_PROCESS).build();
		Long followedId = 1L;

		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(userWhoAuth);
		when(followDao.findOneByFollowedIdAndFollowerId(followedId, userWhoAuth.getId()))
				.thenReturn(Optional.of(follow));

		assertNotNull(followService.getFollowStatusByFollowedId(1L));

	}

	// getFollowStatusByFollowerId
	@Test
	void getFollowStatusByFollowerIdParamIdNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> followService.getFollowStatusByFollowerId(null));
	}

	@Test
	void getFollowStatusByFollowerIdReturnsNotNull() {
		User userWhoAuth = User.builder().id(2L).visible(false).build();
		Follow follow = Follow.builder().followStatus(FollowStatus.IN_PROCESS).build();
		Long followerId = 1L;

		// auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(userWhoAuth);
		// follow dao
		when(followDao.findOneByFollowedIdAndFollowerId(userWhoAuth.getId(), followerId))
				.thenReturn(Optional.of(follow));

		assertNotNull(followService.getFollowStatusByFollowerId(followerId));
	}

	// countByFollowStatusAndFollowed

	@Test

	void countByFollowStatusAndFollowedParamFollowStatusNullThrow() {
		Long followedId = 1L;
		assertThrows(IllegalArgumentException.class,
				() -> followService.countByFollowStatusAndFollowed(null, followedId));
		verify(followDao, never()).countByFollowedIdAndFollowStatus(followedId, null);
	}

	@Test
	void countByFollowStatusAndFollowedParamFollowedIdNullThrow() {
		assertThrows(IllegalArgumentException.class,
				() -> followService.countByFollowStatusAndFollowed(FollowStatus.ACCEPTED, null));
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

	// countByFollowStatusAndFollower

	@Test
	void countByFollowStatusAndFollowerParamFollowStatusNullThrow() {
		Long followerId = 1L;
		assertThrows(IllegalArgumentException.class,
				() -> followService.countByFollowStatusAndFollower(null, followerId));
		verify(followDao, never()).countByFollowedIdAndFollowStatus(followerId, null);
	}

	@Test
	void countByFollowStatusAndFollowerParamFollowedIdNullThrow() {
		assertThrows(IllegalArgumentException.class,
				() -> followService.countByFollowStatusAndFollower(FollowStatus.ACCEPTED, null));
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
