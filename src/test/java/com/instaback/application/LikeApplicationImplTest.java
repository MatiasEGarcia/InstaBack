package com.instaback.application;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.instaback.dto.request.ReqLike;
import com.instaback.dto.response.PublicatedImageDto;
import com.instaback.entity.PublicatedImage;
import com.instaback.entity.User;
import com.instaback.enums.RolesEnum;
import com.instaback.enums.TypeItemLikedEnum;
import com.instaback.exception.InvalidActionException;
import com.instaback.mapper.LikeMapper;
import com.instaback.mapper.PublicatedImageMapper;
import com.instaback.service.LikeService;
import com.instaback.service.PublicatedImageService;
import com.instaback.util.MessagesUtils;

@ExtendWith(MockitoExtension.class)
class LikeApplicationImplTest {
	
	@Mock private Authentication auth;
	@Mock private SecurityContext securityContext;
	@Mock private LikeService lService;
	@Mock private PublicatedImageService pImaService;
	@Mock private LikeMapper lMapper;
	@Mock private PublicatedImageMapper pMapper;
	@Mock private MessagesUtils messUtils;
	@InjectMocks private LikeApplicationImpl lApplication;
	private final User authUser = User.builder().id(1L).username("Mati").role(RolesEnum.ROLE_USER).build();

	
	//save
	@Test
	void saveParamReqLikeNullThrow() {
		assertThrows(IllegalArgumentException.class , () -> lApplication.save(null));
	}
	
	@Test
	void saveLikeAlreadyExistsThrow() {
		ReqLike reqLike = new ReqLike();
		reqLike.setItemId(1L);
		reqLike.setType(TypeItemLikedEnum.PULICATED_IMAGE);
		
		//auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(authUser);
		when(lService.exist(reqLike.getType(), reqLike.getItemId(), authUser.getId())).thenReturn(true);
		
		assertThrows(InvalidActionException.class, ()-> lApplication.save(reqLike));
		
		verify(lService, never()).save(reqLike.getItemId(), reqLike.getDecision(), reqLike.getType(), authUser);
	}
	
	@Test
	void savePublicatedImageReturnNotNull() {
		ReqLike reqLike = new ReqLike();
		reqLike.setItemId(1L);
		reqLike.setDecision(true);
		reqLike.setType(TypeItemLikedEnum.PULICATED_IMAGE);
		PublicatedImage p = new PublicatedImage(1L);
		Long positiveNegativeLikesNumber = 5L;
		
		//auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(authUser);
		//check if like exists
		when(lService.exist(reqLike.getType(), reqLike.getItemId(), authUser.getId())).thenReturn(false);
		//getting publication
		when(pImaService.getById(reqLike.getItemId())).thenReturn(p);
		//map publicatedImage
		when(pMapper.publicatedImageToPublicatedImageDto(p)).thenReturn(new PublicatedImageDto());
		//getting positive and negative likes number
		when(lService.getLikesNumberByItemIdAndDecision(reqLike.getItemId(), true)).thenReturn(positiveNegativeLikesNumber);
		when(lService.getLikesNumberByItemIdAndDecision(reqLike.getItemId(), false)).thenReturn(positiveNegativeLikesNumber);
		
		assertNotNull(lApplication.save(reqLike));
		verify(lService).save(reqLike.getItemId(), reqLike.getDecision(), reqLike.getType(), authUser);
	}
	
	//deleteByPublicatedImageId
	@Test
	void deleteByPublicatedImageIdReturnNotNull() {
		Long pId = 1L;
		PublicatedImage pFound = new PublicatedImage();
		PublicatedImageDto pDto = new PublicatedImageDto();
		
		when(pImaService.getById(pId)).thenReturn(pFound);
		when(pMapper.publicatedImageToPublicatedImageDto(pFound)).thenReturn(pDto);
		 
		assertNotNull(lApplication.deleteByPublicatedImageId(pId));
		verify(lService).deleteByItemId(pId);
	}
	

}
