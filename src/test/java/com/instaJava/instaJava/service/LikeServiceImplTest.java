package com.instaJava.instaJava.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.instaJava.instaJava.dao.LikeDao;
import com.instaJava.instaJava.dto.request.ReqLike;
import com.instaJava.instaJava.dto.request.ReqSearch;
import com.instaJava.instaJava.entity.Like;
import com.instaJava.instaJava.entity.PublicatedImage;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.enums.GlobalOperationEnum;
import com.instaJava.instaJava.enums.RolesEnum;
import com.instaJava.instaJava.enums.TypeItemLikedEnum;
import com.instaJava.instaJava.util.MessagesUtils;

@ExtendWith(MockitoExtension.class)
class LikeServiceImplTest {

	@Mock private Authentication auth;
	@Mock private SecurityContext securityContext;
	@Mock private Clock clock;
	@Mock private LikeDao likeDao;
	@Mock private PublicatedImageService publiImaService;
	@Mock private MessagesUtils messUtils;
	@Mock private SpecificationService<Like> specService;
	@InjectMocks private LikeServiceImpl likeService;
	private User user = User.builder()
			.userId(1L)
			.username("random")
			.password("random")
			.role(RolesEnum.ROLE_USER)
			.build();
	
	
	@Test
	void deleteByIdLikeIdNullThrow() {
		assertThrows(IllegalArgumentException.class,
				() -> likeService.deleteById(null));
	}
	@Test
	void deleteByIdLikeNoExistsReturn0() {
		when(likeDao.findById(any(Long.class))).thenReturn(Optional.empty());
		assertEquals(0, likeService.deleteById(any(Long.class)));
		verify(likeDao,never()).delete(any(Like.class));
	}
	@Test
	void deleteByIdLikeOwnerNotSameThanAuthenticatedReturn0() {
		Like like = Like.builder().ownerLike(User.builder().build()).build();
		when(likeDao.findById(any(Long.class))).thenReturn(Optional.of(like));
		//set the user , when the method want to access to the user authenticated
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
						.thenReturn(user);
		assertEquals(0, likeService.deleteById(any(Long.class)));
		verify(likeDao,never()).delete(any(Like.class));
	}
	@Test
	void deleteByIdReturn1() {
		Like like = Like.builder().ownerLike(user).build();
		when(likeDao.findById(any(Long.class))).thenReturn(Optional.of(like));
		//set the user , when the method want to access to the user authenticated
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
						.thenReturn(user);
		assertEquals(1, likeService.deleteById(any(Long.class)));
		verify(likeDao).delete(any(Like.class));
	}
	
	
	@Test
	void existTypeNullThrow() {
		assertThrows(IllegalArgumentException.class ,
				() -> likeService.exist(null,1L,1L));
	}
	@Test
	void existItemIdNullThrow() {
		assertThrows(IllegalArgumentException.class ,
				() -> likeService.exist(TypeItemLikedEnum.PULICATED_IMAGE,null,1L));
	}
	@Test
	void existOwnerLikeIdNullThrow() {
		assertThrows(IllegalArgumentException.class ,
				() -> likeService.exist(TypeItemLikedEnum.PULICATED_IMAGE,1L,null));
	}
	@SuppressWarnings("unchecked")
	@Test
	void existReturnBooleanNotNull() {
		Specification<Like> spec = (root,query,criteriaBuilder) -> criteriaBuilder.equal(root.get("random"), "someRandom");
		when(specService.getSpecification(any(List.class),eq(GlobalOperationEnum.AND))).thenReturn(spec);
		when(likeDao.exists(spec)).thenReturn(true);
		assertTrue(likeService.exist(TypeItemLikedEnum.PULICATED_IMAGE,1L,1L));
	}
	
	
	@Test
	void saveAllReqLikeListNullThrow() {
		assertThrows(IllegalArgumentException.class,
				() -> likeService.saveAll(null));
	}
	@Test
	void saveAllReqLikeListEmptyReturnEmpty() {
		assertTrue(likeService.saveAll(Collections.emptyList()).isEmpty());
	}
	@SuppressWarnings("unchecked")
	@Test
	void saveAllPubliImageNoOneValidItemNoExistsReturnEmptyList() {
		Long itemId = 1L;
		//won't be valid because the item no exists
		ReqLike reqLike = ReqLike.builder().type(TypeItemLikedEnum.PULICATED_IMAGE)
				.itemId(itemId).decision(true).build();
		//set the user , when the method want to access to the user authenticated
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
				.thenReturn(user);
		//set the clock values //random
		when(clock.getZone()).thenReturn(
				ZoneId.of("Europe/Prague"));
		when(clock.instant()).thenReturn(
				Instant.parse("2020-12-01T10:05:23.653Z"));
		
		when(publiImaService.getById(any(Long.class))).thenReturn(Optional.empty());//return empty so no exist the item
		assertTrue(likeService.saveAll(List.of(reqLike)).isEmpty(),"returned list should be empty");
		verify(likeDao,never()).saveAll(any(List.class));
	}
	@SuppressWarnings("unchecked")
	@Test
	void saveAllPubliImageNoOneValidLikeAlreadyExistsReturnEmptyList() {
		PublicatedImage publiImage = PublicatedImage.builder().build();
		Long itemId = 1L;
		//won't be valid because the like already exists
		ReqLike reqLike = ReqLike.builder().type(TypeItemLikedEnum.PULICATED_IMAGE)
				.itemId(itemId).decision(true).build();
		
		LikeServiceImpl likeServiceSpy = spy(likeService);
		
		//set the user , when the method want to access to the user authenticated
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
				.thenReturn(user);
		//set the clock values //random
		when(clock.getZone()).thenReturn(
				ZoneId.of("Europe/Prague"));
		when(clock.instant()).thenReturn(
				Instant.parse("2020-12-01T10:05:23.653Z"));
		when(publiImaService.getById(any(Long.class))).thenReturn(Optional.of(publiImage));//item exists
		//spy exist method in likeService
		doReturn(true).when(likeServiceSpy) //return true so the like already exists
				.exist(TypeItemLikedEnum.PULICATED_IMAGE, itemId, user.getUserId());
		
		assertTrue(likeServiceSpy.saveAll(List.of(reqLike)).isEmpty(),"returned list should be empty");
		verify(likeDao,never()).saveAll(any(List.class));
	}
	@SuppressWarnings("unchecked")
	@Test
	void saveAllPubliImageReturnListNotEmpty() {
		PublicatedImage publiImage = PublicatedImage.builder().build();
		Long itemId = 1L;
		//won't be valid because the like already exists
		ReqLike reqLike = ReqLike.builder().type(TypeItemLikedEnum.PULICATED_IMAGE)
				.itemId(itemId).decision(true).build();
		
		LikeServiceImpl likeServiceSpy = spy(likeService);
		
		//set the user , when the method want to access to the user authenticated
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
				.thenReturn(user);
		//set the clock values //random
		when(clock.getZone()).thenReturn(
				ZoneId.of("Europe/Prague"));
		when(clock.instant()).thenReturn(
				Instant.parse("2020-12-01T10:05:23.653Z"));
		when(publiImaService.getById(any(Long.class))).thenReturn(Optional.of(publiImage));//item exists
		when(likeDao.saveAll(any(List.class))).thenReturn(List.of(Like.builder().build()));
		//spy exist method in likeService
		doReturn(false).when(likeServiceSpy) //return false so the like no exists
				.exist(TypeItemLikedEnum.PULICATED_IMAGE, itemId, user.getUserId());
		
		assertFalse(likeServiceSpy.saveAll(List.of(reqLike)).isEmpty());
		verify(likeDao).saveAll(any(List.class));
	}
	
	
	@Test
	void saveReqLikeNullThrow() {
		assertThrows(IllegalArgumentException.class,
				() -> likeService.save(null));
	}
	@Test
	void saveReqLikeNoValidItemReturnEmptyOptional() {
		Long itemId = 1L;
		//won't be valid because the item no exists
		ReqLike reqLike = ReqLike.builder().type(TypeItemLikedEnum.PULICATED_IMAGE)
				.itemId(itemId).decision(true).build();
		//set the user , when the method want to access to the user authenticated
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
						.thenReturn(user);
		when(publiImaService.getById(any(Long.class))).thenReturn(Optional.empty());//return empty so no exist the item
		
		Optional<Like> optLike = likeService.save(reqLike);
		if(optLike.isPresent()) 
			fail("if the reqLike has a item that no exist, return optional empty");
		verify(likeDao,never()).save(any(Like.class));
	}
	@Test
	void saveReqLikeNoValidLikeAlreadyExistsReturnEmptyOptional() {
		PublicatedImage publiImage = PublicatedImage.builder().build();
		Long itemId = 1L;
		//won't be valid because the item no exists
		ReqLike reqLike = ReqLike.builder().type(TypeItemLikedEnum.PULICATED_IMAGE)
				.itemId(itemId).decision(true).build();
		//set the user , when the method want to access to the user authenticated
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
						.thenReturn(user);
		when(publiImaService.getById(any(Long.class))).thenReturn(Optional.of(publiImage));//the item exists
		
		LikeServiceImpl likeServiceSpy = spy(likeService);
		//spy exist method in likeService
		doReturn(true).when(likeServiceSpy) //return true so the like exists
					.exist(TypeItemLikedEnum.PULICATED_IMAGE, itemId, user.getUserId());
		
		Optional<Like> optLike = likeServiceSpy.save(reqLike);
		if(optLike.isPresent()) 
			fail("if the like already exists , return optional empty");
		verify(likeDao,never()).save(any(Like.class));
	}
	@Test
	void saveReqLikeValidReturnPresentOptional() {
		PublicatedImage publiImage = PublicatedImage.builder().build();
		Long itemId = 1L;
		//won't be valid because the item no exists
		ReqLike reqLike = ReqLike.builder().type(TypeItemLikedEnum.PULICATED_IMAGE)
				.itemId(itemId).decision(true).build();
		//set the user , when the method want to access to the user authenticated
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
						.thenReturn(user);
		when(publiImaService.getById(any(Long.class))).thenReturn(Optional.of(publiImage));//the item exists
		
		LikeServiceImpl likeServiceSpy = spy(likeService);
		//spy exist method in likeService
		doReturn(false).when(likeServiceSpy) //return false so the like no exists
					.exist(TypeItemLikedEnum.PULICATED_IMAGE, itemId, user.getUserId());
		
		//set the clock values //random
		when(clock.getZone()).thenReturn(
					ZoneId.of("Europe/Prague"));
		when(clock.instant()).thenReturn(
					Instant.parse("2020-12-01T10:05:23.653Z"));
		
		when(likeDao.save(any(Like.class))).thenReturn(Like.builder().build());
		Optional<Like> optLike = likeServiceSpy.save(reqLike);
		if(optLike.isEmpty()) 
			fail("return should be optional present");
		verify(likeDao).save(any(Like.class));
	}
	
	
	@Test
	void getPositiveAndNegativeLikesByItemIdArgNullThrow() {
		assertThrows(IllegalArgumentException.class,() -> likeService.getPositiveAndNegativeLikesByItemId(null));
	}
	@Test
	void getPositiveAndNegativeLikesByItemId() {
		//only decision matters here
		Like positiveLike = Like.builder().decision(true).build();
		Like negativeLike = Like.builder().decision(false).build();
		
		Specification<Like> spec = (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("itemId"), "1");
		when(specService.getSpecification(any(ReqSearch.class))).thenReturn(spec);
		when(likeDao.findAll(spec)).thenReturn(List.of(positiveLike,negativeLike));
		Map<String,String> map= likeService.getPositiveAndNegativeLikesByItemId(anyLong()); 
		if( !map.get("Positive").equalsIgnoreCase("1") || !map.get("Negative").equalsIgnoreCase("1")) 
			fail("There should be 1 Positive like and 1 Negative");
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
