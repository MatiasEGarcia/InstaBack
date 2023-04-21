package com.instaJava.instaJava.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
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
import com.instaJava.instaJava.entity.Like;
import com.instaJava.instaJava.entity.PublicatedImage;
import com.instaJava.instaJava.entity.User;
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
	@InjectMocks private LikeServiceImpl likeService;
	private final User user = User.builder()
			.username("random")
			.password("random")
			.role(RolesEnum.ROLE_USER)
			.build();
	
	@Test
	void saveTypeNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> likeService.save(null, 1L, false));
	}
	@Test
	void saveItemIdNullThrow() {
		assertThrows(IllegalArgumentException.class, 
				() -> likeService.save(TypeItemLikedEnum.PULICATED_IMAGE, null, false));
	}
	@Test
	void saveIdPulblicatedImageItemNoExistReturnOptionalEmpty() {
		when(publiImaService.getById(1L)).thenReturn(Optional.empty());
		Optional<Like> optL = likeService.save(TypeItemLikedEnum.PULICATED_IMAGE, 1L, true);
		if(optL.isPresent()) fail("if the publicated image no exist, then should return empty like instead save the like");
		verify(publiImaService).getById(1L);
	}
	
	@Test
	void saveReturnOptionalPresent() {
		Long id = 1L;
		boolean decision = true;
		//set clock for test
		when(clock.getZone()).thenReturn(
				ZoneId.of("Europe/Prague"));
		when(clock.instant()).thenReturn(
				Instant.parse("2020-12-01T10:05:23.653Z"));
		Like like = Like.builder()
				.itemId(id)
				.decision(decision)
				.itemType(TypeItemLikedEnum.PULICATED_IMAGE)
				.likedAt(ZonedDateTime.now(clock))
				.ownerLike(user)
				.build();
		//set the user , when the method want to access to the user authenticated
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
				.thenReturn(user);
		
		when(publiImaService.getById(1L)).thenReturn(Optional.of(PublicatedImage.builder().build()));
		when(likeDao.save(like)).thenReturn(like);
		Optional<Like> optL =  likeService.save(TypeItemLikedEnum.PULICATED_IMAGE, id, decision);
		if(optL.isEmpty()) fail("if the publicated image exist, should return present like");
		verify(publiImaService).getById(id);
		verify(likeDao).save(like);
		
	}

}
