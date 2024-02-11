package com.instaJava.instaJava.application;

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

import com.instaJava.instaJava.dto.request.ReqLike;
import com.instaJava.instaJava.dto.response.LikeDto;
import com.instaJava.instaJava.entity.Like;
import com.instaJava.instaJava.entity.PublicatedImage;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.enums.RolesEnum;
import com.instaJava.instaJava.enums.TypeItemLikedEnum;
import com.instaJava.instaJava.exception.InvalidActionException;
import com.instaJava.instaJava.mapper.LikeMapper;
import com.instaJava.instaJava.service.LikeService;
import com.instaJava.instaJava.service.PublicatedImageService;
import com.instaJava.instaJava.util.MessagesUtils;

@ExtendWith(MockitoExtension.class)
class LikeApplicationImplTest {
	
	@Mock private Authentication auth;
	@Mock private SecurityContext securityContext;
	@Mock private LikeService lService;
	@Mock private PublicatedImageService pImaService;
	@Mock private LikeMapper lMapper;
	@Mock private MessagesUtils messUtils;
	@InjectMocks private LikeApplicationImpl lApplication;
	private final User authUser = User.builder().id(1L).username("Mati").role(RolesEnum.ROLE_USER).build();

	
	//save
	@Test
	void saveParamReqLikeNullThrow() {
		assertThrows(IllegalArgumentException.class , () -> lApplication.save(null));
	}
	
	@Test
	void savePublicatedImageNoExistsThrow() {
		ReqLike reqLike = new ReqLike();
		reqLike.setItemId(1L);
		reqLike.setType(TypeItemLikedEnum.PULICATED_IMAGE);
		
		//auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(authUser);
		when(pImaService.findById(reqLike.getItemId())).thenReturn(Optional.empty());
		
		assertThrows(InvalidActionException.class, ()-> lApplication.save(reqLike));
		
		verify(lService, never()).save(reqLike.getItemId(), reqLike.getDecision(), reqLike.getType(), authUser);
	}
	
	@Test
	void saveLikeAlreadyExistsThrow() {
		ReqLike reqLike = new ReqLike();
		reqLike.setItemId(1L);
		reqLike.setType(TypeItemLikedEnum.PULICATED_IMAGE);
		PublicatedImage p = new PublicatedImage();
		
		//auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(authUser);
		when(pImaService.findById(reqLike.getItemId())).thenReturn(Optional.of(p));
		when(lService.exist(reqLike.getType(), reqLike.getItemId(), authUser.getId())).thenReturn(true);
		
		assertThrows(InvalidActionException.class, ()-> lApplication.save(reqLike));
		
		verify(lService, never()).save(reqLike.getItemId(), reqLike.getDecision(), reqLike.getType(), authUser);
	}
	
	@Test
	void save() {
		ReqLike reqLike = new ReqLike();
		reqLike.setItemId(1L);
		reqLike.setType(TypeItemLikedEnum.PULICATED_IMAGE);
		PublicatedImage p = new PublicatedImage();
		Like likeSaved = new Like();
		
		//auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(authUser);
		//check if item exists
		when(pImaService.findById(reqLike.getItemId())).thenReturn(Optional.of(p));
		//check if like exists
		when(lService.exist(reqLike.getType(), reqLike.getItemId(), authUser.getId())).thenReturn(false);
		//save like
		when(lService.save(reqLike.getItemId(), reqLike.getDecision(), reqLike.getType(), authUser)).thenReturn(likeSaved);
		//map like
		when(lMapper.likeToLikeDto(likeSaved)).thenReturn(new LikeDto());
		
		assertNotNull(lApplication.save(reqLike));
	}
	
	@Test
	void deletePublicationId() {
		Long id = 1L;
		lApplication.deleteByPublicationId(id);
		verify(lService).deleteByPublicationId(id);
	}
	

}
