package com.instaJava.instaJava.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import com.instaJava.instaJava.exception.InvalidException;
import com.instaJava.instaJava.util.MessagesUtils;
import com.instaJava.instaJava.util.PageableUtils;

@ExtendWith(MockitoExtension.class)
class FollowServiceImplTest {

	@Mock private Authentication auth;
	@Mock private SecurityContext securityContext;
	@Mock private UserService userService;
	@Mock private FollowDao followDao;
	@Mock private SpecificationService<Follow> specService;
	@Mock private MessagesUtils messUtils;
	@Mock private PageableUtils pageUtils;
	@InjectMocks private FollowServiceImpl followService;
	
	@Test
	void saveFollowedIdNullThrow() {
		assertThrows(IllegalArgumentException.class,
				() -> followService.save(null));
	}
	@Test
	void saveFollowedNoExistThrow() {
		Long followedId= 1L;
		when(userService.getById(followedId)).thenReturn(Optional.empty());
		assertThrows(IllegalArgumentException.class,
				() -> followService.save(followedId));
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
		Follow follow = Follow.builder()
				.followed(userFollowed)
				.follower(userFollower)
				.followStatus(FollowStatus.ACCEPTED)
				.build();
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
				.thenReturn(userFollower);
		when(userService.getById(userFollowed.getUserId())).thenReturn(Optional.of(userFollowed));
		when(userService.getById(userFollower.getUserId())).thenReturn(Optional.of(userFollower));
		when(followDao.save(follow)).thenReturn(follow);
		assertNotNull(followService.save(userFollowed.getUserId()));
		verify(followDao).save(follow);
		
	}
	
	
	@Test
	void searchReqSearchListArgNullTrow() {
		assertThrows(IllegalArgumentException.class,
				() -> followService.search(any(PageInfoDto.class), null));
	}
	@Test
	void searchPageInfoDtoArgNullThrow() {
		assertThrows(IllegalArgumentException.class,
				() -> followService.search(null, any(ReqSearchList.class)));
	}
	@Test
	void searchPageInfoDtoSortFieldNullThrow() {
		PageInfoDto pageInfoDto = PageInfoDto.builder()
				.pageNo(1)
				.pageSize(10)
				.sortDir(Direction.ASC)
				.build();
		assertThrows(IllegalArgumentException.class,
				() -> followService.search(pageInfoDto, any(ReqSearchList.class)));
	}
	@Test
	void searchPageInfoDtoSortDirNullThrow() {
		PageInfoDto pageInfoDto = PageInfoDto.builder()
				.pageNo(1)
				.pageSize(10)
				.sortField("random")
				.build();
		assertThrows(IllegalArgumentException.class,
				() -> followService.search(pageInfoDto, any(ReqSearchList.class)));
	}
	@Test
	void search() {
		PageInfoDto pageInfoDto = PageInfoDto.builder()
				.pageNo(0)
				.pageSize(10)
				.sortDir(Direction.ASC)
				.sortField("username")
				.build();
		ReqSearch reqSearch = ReqSearch.builder().build();
		ReqSearchList rqSearchList = ReqSearchList.builder()
				.reqSearchs(List.of(reqSearch))
				.globalOperator(GlobalOperationEnum.AND).build();
		//spec for example only, does not match reqSearch
		Specification<Follow> spec = (root,query,criteriaBuilder) -> criteriaBuilder.equal(root.get("followId"), 1);
		when(specService.getSpecification(rqSearchList.getReqSearchs(),GlobalOperationEnum.AND)).thenReturn(spec);
		when(pageUtils.getPageable(pageInfoDto)).thenReturn(Pageable.unpaged());
		when(followDao.findAll(eq(spec), any(Pageable.class))).thenReturn(Page.empty());
		assertNotNull(followService.search(pageInfoDto, rqSearchList));
		verify(followDao).findAll(eq(spec), any(Pageable.class));
	}
	
	
	
	
	@Test
	void updateFollowStatusByIdFollowedNotSameThrow() {
		User userFollowedWhoAuth= User.builder()
				.userId(2L)
				.build();
		User otherUserFollowed = User.builder()
				.userId(3L)
				.build();
		Follow follow = Follow.builder()
				.followed(otherUserFollowed)
				.followStatus(FollowStatus.ACCEPTED)
				.build();
		when(followDao.findById(2L)).thenReturn(Optional.of(follow));
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
				.thenReturn(userFollowedWhoAuth);
		assertThrows(IllegalArgumentException.class,() -> followService.updateFollowStatusById(2L, FollowStatus.ACCEPTED));
		verify(followDao).findById(2L);
		verify(followDao,never()).save(follow);
	}
	@Test
	void updateFollowStatusByIdReturnNotNull() {
		User userFollowedWhoAuth= User.builder()
				.userId(2L)
				.build();
		Follow follow = Follow.builder()
				.followed(userFollowedWhoAuth)
				.followStatus(FollowStatus.ACCEPTED)
				.build();
		when(followDao.findById(2L)).thenReturn(Optional.of(follow));
		when(followDao.save(follow)).thenReturn(follow);
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
				.thenReturn(userFollowedWhoAuth);
		assertNotNull(followService.updateFollowStatusById(2L, FollowStatus.ACCEPTED));
		verify(followDao).findById(2L);
		verify(followDao).save(follow);
	}
	@Test
	void updateFollowStatusByIdArgFollowStatusNullThrow() {
		assertThrows(IllegalArgumentException.class,() -> followService.updateFollowStatusById(1L, null));
	}
	
	
	
	
	@Test
	void findByIdNoExistThrow() {
		when(followDao.findById(1L)).thenReturn(Optional.empty());
		assertThrows(InvalidException.class, () ->followService.findById(1L));
	}
	@Test
	void findByIdExistReturnNotNull() {
		when(followDao.findById(1L)).thenReturn(Optional.of(new Follow()));
		assertNotNull(followService.findById(1L));
	}
	
	
	@Test
	void deleteByIdThrow() {
		assertThrows(IllegalArgumentException.class, () -> followService.deleteById(null));
	}
	@Test
	void deleteByIdFollowerNotSameThrow() {
		User userWhoAuth= User.builder().userId(2L).visible(false).build();
		User followOwn= User.builder().userId(3L).visible(false).build();
		Follow follow = Follow.builder().follower(followOwn).build(); //I will only compare follower owner with the auth user.
		FollowServiceImpl follSerSpy = spy(followService);
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
				.thenReturn(userWhoAuth);
		doReturn(follow).when(follSerSpy).findById(anyLong());
		assertThrows(InvalidException.class,() ->  follSerSpy.deleteById(anyLong()),"if followOwner is not the same than"
				+ " the authenticated user , the follow cannot be deleted");
	}
	@Test
	void deleteById() {
		User userWhoAuth= User.builder().userId(2L).visible(false).build();
		Follow follow = Follow.builder().follower(userWhoAuth).build(); 
		FollowServiceImpl follSerSpy = spy(followService);
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
				.thenReturn(userWhoAuth);
		doReturn(follow).when(follSerSpy).findById(anyLong());
		follSerSpy.deleteById(anyLong());
		verify(followDao).delete(follow);
	}
	
	
	@Test
	void getFollowStatusByFollowedIdIdNullThrow() {
		assertThrows(IllegalArgumentException.class,
				() -> followService.getFollowStatusByFollowedId(null));
	}
	@Test 
	void getFollowStatusByFollowedIdUserNoExistThrow(){
		when(userService.getById(1L)).thenReturn(Optional.empty());
		assertThrows(IllegalArgumentException.class, 
				() -> followService.getFollowStatusByFollowedId(1L));
		verify(followDao,never()).findOne(any(Specification.class));
	}
	@Test
	void getFollowStatusByFollowedIdUserVisibleReturnAccepted() {
		User user = User.builder().visible(true).build();
		when(userService.getById(1L)).thenReturn(Optional.of(user));
		FollowStatus followStatus = followService.getFollowStatusByFollowedId(1L);
		if(!followStatus.equals(FollowStatus.ACCEPTED)) fail("if the user is visible/public true then should return accepted");
		verify(followDao,never()).findOne(any(Specification.class));
	}
	@Test
	void getFollowStatusByFollowedIdUserNoVisibleFollowedRecordNoExistReturnNotAsked() {
		User userWhoAuth= User.builder().userId(2L).visible(false).build();
		//spec not match, is noly for example
		Specification<Follow> spec = (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("followId"), 1L);
		
		when(userService.getById(1L)).thenReturn(Optional.of(userWhoAuth));
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
				.thenReturn(userWhoAuth);
		when(specService.getSpecification(any(List.class),eq(GlobalOperationEnum.AND))).thenReturn(spec);
		when(followDao.findOne(spec)).thenReturn(Optional.empty());
		FollowStatus followStatus = followService.getFollowStatusByFollowedId(1L);
		if(!followStatus.equals(FollowStatus.NOT_ASKED)) fail("if the follower record no exist should return followStatus NOT_ASKED");
	}
	@Test
	void getFollowStatusByFollowedIdUserNoVisibleFollowedRecordExistReturnNotNull() {
		User userWhoAuth= User.builder().userId(2L).visible(false).build();
		Follow follower = Follow.builder().followStatus(FollowStatus.IN_PROCESS).build();
		//spec not match, is noly for example
		Specification<Follow> spec = (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("followId"), 1L);
		when(userService.getById(1L)).thenReturn(Optional.of(userWhoAuth));
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
				.thenReturn(userWhoAuth);
		when(specService.getSpecification(any(List.class),eq(GlobalOperationEnum.AND))).thenReturn(spec);
		when(followDao.findOne(spec)).thenReturn(Optional.of(follower));
		assertNotNull(followService.getFollowStatusByFollowedId(1L));
	}
	
	
	@Test
	void countFollowedByUserIdArgNull() {
		assertThrows(IllegalArgumentException.class,
				()-> followService.countFollowedByUserId(null));
	}
	@Test
	void countFollowedByUserIdReturnNotNull() {
		Specification<Follow> spec = (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("follower"), 1L);
		when(specService.getSpecification(any(ReqSearch.class))).thenReturn(spec);
		when(followDao.count(spec)).thenReturn(1L);
		assertNotNull(followService.countFollowedByUserId(1L));
	}
	
	
	@Test
	void countFollowerByUserIdArgNull() {
		assertThrows(IllegalArgumentException.class,
				()-> followService.countFollowerByUserId(null));
	}
	@Test
	void countFollowerByUserIdReturnNotNull() {
		Specification<Follow> spec = (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("followed"), 1L);
		when(specService.getSpecification(any(ReqSearch.class))).thenReturn(spec);
		when(followDao.count(spec)).thenReturn(1L);
		assertNotNull(followService.countFollowerByUserId(1L));
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
