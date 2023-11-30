package com.instaJava.instaJava.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.instaJava.instaJava.dao.LikeDao;
import com.instaJava.instaJava.dto.UserDto;
import com.instaJava.instaJava.dto.request.ReqLike;
import com.instaJava.instaJava.dto.response.LikeDto;
import com.instaJava.instaJava.entity.Like;
import com.instaJava.instaJava.entity.PublicatedImage;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.enums.RolesEnum;
import com.instaJava.instaJava.enums.TypeItemLikedEnum;
import com.instaJava.instaJava.exception.InvalidException;
import com.instaJava.instaJava.exception.RecordNotFoundException;
import com.instaJava.instaJava.mapper.LikeMapper;
import com.instaJava.instaJava.util.MessagesUtils;

@ExtendWith(MockitoExtension.class)
class LikeServiceImplTest {

	@Mock
	private Authentication auth;
	@Mock
	private SecurityContext securityContext;
	@Mock
	private Clock clock;
	@Mock
	private LikeDao likeDao;
	@Mock
	private LikeMapper likeMapper;
	@Mock
	private PublicatedImageService publiImaService;
	@Mock
	private MessagesUtils messUtils;
	@Mock
	private SpecificationService<Like> specService;
	@InjectMocks
	private LikeServiceImpl likeService;
	private User user = User.builder().userId(1L).username("random").password("random").role(RolesEnum.ROLE_USER)
			.build();

	// deleteById
	@Test
	void deleteByIdLikeIdNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> likeService.deleteById(null));
	}

	@Test
	void deleteByIdLikeNoExistsThrow() {
		when(likeDao.findById(any(Long.class))).thenReturn(Optional.empty());
		assertThrows(RecordNotFoundException.class, () -> likeService.deleteById(1L));
		verify(likeDao,never()).delete(any(Like.class));
	}

	@Test
	void deleteByIdLikeOwnerNotSameThanAuthenticatedThrow() {
		Like like = Like.builder().ownerLike(User.builder().build()).build();

		when(likeDao.findById(any(Long.class))).thenReturn(Optional.of(like));
		// set the user , when the method want to access to the user authenticated
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);

		assertThrows(InvalidException.class, () -> likeService.deleteById(1L));

		verify(likeDao, never()).delete(any(Like.class));
	}

	@Test
	void deleteBy() {
		Like like = Like.builder().ownerLike(user).build();

		when(likeDao.findById(any(Long.class))).thenReturn(Optional.of(like));
		// set the user , when the method want to access to the user authenticated
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);

		likeService.deleteById(1L);

		verify(likeDao).delete(any(Like.class));
	}

//exist
	@Test
	void existTypeNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> likeService.exist(null, 1L, 1L));
	}

	@Test
	void existItemIdNullThrow() {
		assertThrows(IllegalArgumentException.class,
				() -> likeService.exist(TypeItemLikedEnum.PULICATED_IMAGE, null, 1L));
	}

	@Test
	void existOwnerLikeIdNullThrow() {
		assertThrows(IllegalArgumentException.class,
				() -> likeService.exist(TypeItemLikedEnum.PULICATED_IMAGE, 1L, null));
	}

	@Test
	void existReturnTrue() {
		when(likeDao.existsByItemTypeAndItemIdAndOwnerLike(TypeItemLikedEnum.PULICATED_IMAGE, 1L, 1L)).thenReturn(true);
		
		assertTrue(likeService.exist(TypeItemLikedEnum.PULICATED_IMAGE, 1L, 1L));
	}

	@Test
	void existReturnFalse() {
		when(likeDao.existsByItemTypeAndItemIdAndOwnerLike(TypeItemLikedEnum.PULICATED_IMAGE, 1L, 1L)).thenReturn(false);
		
		assertFalse(likeService.exist(TypeItemLikedEnum.PULICATED_IMAGE, 1L, 1L));
	}

	// save
	@Test
	void saveParamReqLikeNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> likeService.save(null));
	}

	@Test
	void saveParamReqLikeItemIdNullThrow() {
		ReqLike reqLike = ReqLike.builder().type(TypeItemLikedEnum.PULICATED_IMAGE).build();
		assertThrows(IllegalArgumentException.class, () -> likeService.save(reqLike));
	}

	@Test
	void saveParamReqLikeTypeNullThrow() {
		ReqLike reqLike = ReqLike.builder().itemId(1L).build();
		assertThrows(IllegalArgumentException.class, () -> likeService.save(reqLike));
	}

	@Test
	void saveLikeTypePublicatedImageNoExistSoNotValidThrow() {
		User authUser = User.builder() // who is authenticated and wants to create follow record.
				.userId(2L).build();
		ReqLike reqLike = ReqLike.builder().itemId(1L).type(TypeItemLikedEnum.PULICATED_IMAGE).build();

		// setting authenticated user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(authUser);

		// checking item existence.
		when(publiImaService.findById(reqLike.getItemId())).thenReturn(Optional.empty());

		assertThrows(InvalidException.class, () -> likeService.save(reqLike));

		verify(likeDao, never()).save(any(Like.class));
	}

	@Test
	void saveLikeTypePublicatedImageExistButLikeRecordAlreadyExistsSoNotValidThrow() {
		User authUser = User.builder() // who is authenticated and wants to create follow record.
				.userId(2L).build();
		ReqLike reqLike = ReqLike.builder().itemId(1L).type(TypeItemLikedEnum.PULICATED_IMAGE).build();
		LikeServiceImpl spyLikeService = spy(likeService);
		PublicatedImage pubImage = new PublicatedImage();

		// setting authenticated user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(authUser);

		// checking item existence.
		when(publiImaService.findById(reqLike.getItemId())).thenReturn(Optional.of(pubImage));
		// cheking if like already exist (in this test exist)
		doReturn(true).when(spyLikeService).exist(reqLike.getType(), reqLike.getItemId(), authUser.getUserId());

		assertThrows(InvalidException.class, () -> spyLikeService.save(reqLike));

		verify(likeDao, never()).save(any(Like.class));
	}

	@Test
	void saveLikeTypePublicatedImage() {
		Long itemId = 2L;
		TypeItemLikedEnum itemType = TypeItemLikedEnum.PULICATED_IMAGE;
		User authUser = User.builder() // who is authenticated and wants to create follow record.
				.userId(2L).build();
		UserDto userDto = UserDto.builder()
				.userId("2")
				.build();
		ReqLike reqLike = ReqLike.builder().itemId(itemId).type(itemType).build();
		LikeServiceImpl spyLikeService = spy(likeService);
		PublicatedImage pubImage = new PublicatedImage();
		

		// setting authenticated user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(authUser);

		// checking item existence.
		when(publiImaService.findById(reqLike.getItemId())).thenReturn(Optional.of(pubImage));
		// cheking if like already exist (in this test exist)
		doReturn(false).when(spyLikeService).exist(reqLike.getType(), reqLike.getItemId(), authUser.getUserId());

		// set the clock values //random
		when(clock.getZone()).thenReturn(ZoneId.of("Europe/Prague"));
		when(clock.instant()).thenReturn(Instant.parse("2020-12-01T10:05:23.653Z"));
		
		//I need to set clock returns, that is why I create this object after when block
		Like likeToSave = Like.builder()
				.itemId(itemId)
				.itemType(itemType)
				.ownerLike(authUser)
				.likedAt(ZonedDateTime.now(clock))
				.build();
		LikeDto likeDto = LikeDto.builder()
				.itemId(itemId.toString())
				.itemType(itemType)
				.ownerLike(userDto)
				.likedAt(ZonedDateTime.now(clock))
				.build();
		
		when(likeDao.save(likeToSave)).thenReturn(likeToSave);

		when(likeMapper.likeToLikeDto(likeToSave)).thenReturn(likeDto);

		assertNotNull(spyLikeService.save(reqLike));

		verify(likeDao).save(any(Like.class));
	}
}
