package com.instaJava.instaJava.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.instaJava.instaJava.dao.LikeDao;
import com.instaJava.instaJava.dto.dao.IdValueDto;
import com.instaJava.instaJava.entity.Like;
import com.instaJava.instaJava.entity.PublicatedImage;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.enums.RolesEnum;
import com.instaJava.instaJava.enums.TypeItemLikedEnum;
import com.instaJava.instaJava.exception.InvalidActionException;
import com.instaJava.instaJava.exception.RecordNotFoundException;
import com.instaJava.instaJava.mapper.LikeMapper;
import com.instaJava.instaJava.util.MessagesUtils;
import com.instaJava.instaJava.util.SearchsUtils;

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
	private SearchsUtils searchUtils;
	@Mock
	private SpecificationService<Like> specService;
	@InjectMocks
	private LikeServiceImpl likeService;
	private User user = User.builder().id(1L).username("random").password("random").role(RolesEnum.ROLE_USER)
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

		assertThrows(InvalidActionException.class, () -> likeService.deleteById(1L));

		verify(likeDao, never()).delete(any(Like.class));
	}

	@Test
	void deleteById() {
		Like like = Like.builder().ownerLike(user).build();

		when(likeDao.findById(any(Long.class))).thenReturn(Optional.of(like));
		// set the user , when the method want to access to the user authenticated
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);

		likeService.deleteById(1L);

		verify(likeDao).delete(any(Like.class));
	}

	//deleteByItemId
	@Test
	void deleteByItemIdIdItemIdNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> likeService.deleteByItemId(null));
	}
	
	@Test
	void deleteByItemIdLikeNotFound() {
		Long publicationId = 1L;
		
		//auth
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		//like dao
		when(likeDao.getByItemIdAndOwnerLikeId(publicationId, user.getId())).thenReturn(Optional.empty());
		assertThrows(RecordNotFoundException.class, () -> likeService.deleteByItemId(publicationId));
		verify(likeDao,never()).delete(any(Like.class));
	}
	
	@Test
	void deleteByItemId() {
		Long publicationId = 1L;
		Like likeToDelete = new Like();
		
		//auth
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		//like dao
		when(likeDao.getByItemIdAndOwnerLikeId(publicationId, user.getId())).thenReturn(Optional.of(likeToDelete));
		likeService.deleteByItemId(publicationId);
		verify(likeDao).delete(likeToDelete);
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
		when(likeDao.existsByItemTypeAndItemIdAndOwnerLikeId(TypeItemLikedEnum.PULICATED_IMAGE, 1L, 1L)).thenReturn(true);
		
		assertTrue(likeService.exist(TypeItemLikedEnum.PULICATED_IMAGE, 1L, 1L));
	}

	@Test
	void existReturnFalse() {
		when(likeDao.existsByItemTypeAndItemIdAndOwnerLikeId(TypeItemLikedEnum.PULICATED_IMAGE, 1L, 1L)).thenReturn(false);
		
		assertFalse(likeService.exist(TypeItemLikedEnum.PULICATED_IMAGE, 1L, 1L));
	}

	// save
	@Test
	void saveParamItemIdNullThrow() {
		Long itemId = null;
		Boolean decision = true;
		TypeItemLikedEnum type = TypeItemLikedEnum.PULICATED_IMAGE;
		User userOwner = new User();
		
		assertThrows(IllegalArgumentException.class, () -> likeService.save(itemId, decision, type, userOwner));
		verify(likeDao, never()).save(any(Like.class));
	}
	
	@Test
	void saveParamDecisionNullThrow() {
		Long itemId = 1L;
		Boolean decision = null;
		TypeItemLikedEnum type = TypeItemLikedEnum.PULICATED_IMAGE;
		User userOwner = new User();
		
		assertThrows(IllegalArgumentException.class, () -> likeService.save(itemId, decision, type, userOwner));
		verify(likeDao, never()).save(any(Like.class));
	}
	
	@Test
	void saveParamTypeNullThrow() {
		Long itemId = 1L;
		Boolean decision = true;
		TypeItemLikedEnum type = null;
		User userOwner = new User();
		
		assertThrows(IllegalArgumentException.class, () -> likeService.save(itemId, decision, type, userOwner));
		verify(likeDao, never()).save(any(Like.class));
	}
	
	@Test
	void saveParamTypeUserThrow() {
		Long itemId = 1L;
		Boolean decision = true;
		TypeItemLikedEnum type = TypeItemLikedEnum.PULICATED_IMAGE;
		User userOwner = null;
		
		assertThrows(IllegalArgumentException.class, () -> likeService.save(itemId, decision, type, userOwner));
		verify(likeDao, never()).save(any(Like.class));
	}
	
	@Test
	void saveReturnNotNull() {
		Long itemId = 1L;
		Boolean decision = true;
		TypeItemLikedEnum type = TypeItemLikedEnum.PULICATED_IMAGE;
		User userOwner = new User(1L);
		
		when(clock.getZone()).thenReturn(ZoneId.of("Europe/Prague"));
		when(clock.instant()).thenReturn(Instant.parse("2020-12-01T10:20:23.653Z"));
		when(likeDao.save(any(Like.class))).thenReturn(new Like());
		assertNotNull(likeService.save(itemId, decision, type, userOwner));
	}
	
	//setItemDecisions
	
	@Test
	void setItemDecisionsParamListItemsNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> likeService.setItemDecisions(null));
	}
	
	@Test
	void setItemDecisionsParamListItemsEmptyThrow() {
		List<PublicatedImage> listItems = new ArrayList<>();
		assertThrows(IllegalArgumentException.class, () -> likeService.setItemDecisions(listItems));
	}
	 
	@Test
	void setItemDecisions() {
		IdValueDto<Boolean> idValueDto = new IdValueDto<Boolean>();
		idValueDto.setId(1L);
		idValueDto.setValue(true);
		PublicatedImage p = new PublicatedImage(1L);
		List<PublicatedImage> listItems = new ArrayList<>();
		Set<Long> setItemsIds = new HashSet<Long>();
		List<IdValueDto<Boolean>> decisions = new ArrayList<>();
		decisions.add(idValueDto);
		
		listItems.add(p);
		setItemsIds.add(p.getId());
		
		//auth
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		//decisions
		when(likeDao.getDecisionsByItemIdAndOwnerLikeId(setItemsIds, user.getId())).thenReturn(decisions);
		when(searchUtils.bynarySearchById(listItems, idValueDto.getId())).thenReturn(0);
		
		likeService.setItemDecisions(listItems);
	}
	
	//@Test
	void setItemDecision() {
		//pending
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
