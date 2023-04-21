package com.instaJava.instaJava.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

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
	/*
	@Test
	void saveIdPulblicatedImageItemNoExistThrow() {
		when(publiImaService.findById(1L)).thenThrow(IllegalArgumentException.class);
		assertThrows(IllegalArgumentException.class, () -> likeService.save(TypeItemLikedEnum.PULICATED_IMAGE, 1L, true));
		verify(publiImaService).findById(1L);
	}
	*/
	
	/*
	@Test
	void saveReturnNotNull() {
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
		
		when(publiImaService.findById(1L)).thenReturn(new PublicatedImage());
		when(likeDao.save(like)).thenReturn(like);
		assertNotNull(likeService.save(TypeItemLikedEnum.PULICATED_IMAGE, id, decision));
		verify(publiImaService).findById(1L);
		verify(likeDao).save(like);
		
	}*/

}
