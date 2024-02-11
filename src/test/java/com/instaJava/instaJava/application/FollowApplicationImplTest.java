package com.instaJava.instaJava.application;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

import com.instaJava.instaJava.dto.FollowDto;
import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.dto.response.ResPaginationG;
import com.instaJava.instaJava.entity.Follow;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.enums.FollowStatus;
import com.instaJava.instaJava.mapper.FollowMapper;
import com.instaJava.instaJava.service.FollowService;
import com.instaJava.instaJava.service.NotificationService;
import com.instaJava.instaJava.service.UserService;

@ExtendWith(MockitoExtension.class)
class FollowApplicationImplTest {

	@Mock private UserService uService;
	@Mock private FollowService fService;
	@Mock private NotificationService nService;
	@Mock private FollowMapper fMapper;
	@InjectMocks private FollowApplicationImpl followApplication;

	//save
	
	@Test
	void saveReturnNotNull() {
		Long followedId = 1L;
		User userFollowed = new User();
		Follow followSaved = new Follow();
		when(uService.findById(followedId)).thenReturn(userFollowed);
		when(fService.save(userFollowed)).thenReturn(followSaved);
		when(fMapper.followToFollowDto(followSaved)).thenReturn(new FollowDto());
		
		assertNotNull(followApplication.save(followedId));
		
		verify(nService).saveNotificationOfFollow(eq(followSaved), anyString());
	}
	
	//search
	@Test
	void searchReturnNotNull() {
		Page<Follow> followPage = Page.empty();
		ResPaginationG<FollowDto> res = new ResPaginationG<>();
		when(fService.search(any(PageInfoDto.class), eq(null))).thenReturn(followPage);
		when(fMapper.pageAndPageInfoDtoToResPaginationG(eq(followPage), any(PageInfoDto.class))).thenReturn(res);
		assertNotNull(followApplication.search(0, 0, null, null, null));
	}
	
	//updateFollowStatusById
	@Test
	void updateFollowStatusByIdReturnNotNull() {
		Follow followUpdate = new Follow();
		Long id = 1L;
		FollowStatus f = FollowStatus.ACCEPTED;
		when(fService.updateFollowStatusById(id, f)).thenReturn(followUpdate);
		when(fMapper.followToFollowDto(followUpdate)).thenReturn(new FollowDto());
		
		assertNotNull(followApplication.updateFollowStatusById(id, f));
	}
	
	//deleteById
	@Test
	void deleteByIdReturnNotNull() {
		Long id = 1L;
		Follow followDeleted = new Follow();
		when(fService.deleteById(id)).thenReturn(followDeleted);
		when(fMapper.followToFollowDto(followDeleted)).thenReturn(new FollowDto());
		assertNotNull(followApplication.deleteById(id));
	}
	
	//deleteByFollwedId
	@Test
	void deleteByFollwedIdReturnNotNull() {
		Long followedId = 1L;
		Follow followDeleted = new Follow();
		when(fService.deleteByFollwedId(followedId)).thenReturn(followDeleted);
		when(fMapper.followToFollowDto(followDeleted)).thenReturn(new FollowDto());
		assertNotNull(followApplication.deleteByFollwedId(followedId));
	}
	
	
	//updateFollowStatusByFollowerId
	@Test
	void updateFollowStatusByFollowerId() {
		Follow fU = new Follow();
		when(fService.updateFollowStatusByFollowerId(null, null)).thenReturn(fU);
		when(fMapper.followToFollowDto(fU)).thenReturn(new FollowDto());
		assertNotNull(followApplication.updateFollowStatusByFollowerId(null, null));
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
