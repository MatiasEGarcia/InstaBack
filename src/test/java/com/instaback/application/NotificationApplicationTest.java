package com.instaback.application;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

import com.instaback.dto.NotificationDto;
import com.instaback.dto.PageInfoDto;
import com.instaback.dto.response.ResPaginationG;
import com.instaback.entity.Notification;
import com.instaback.mapper.NotificationMapper;
import com.instaback.service.NotificationService;


@ExtendWith(MockitoExtension.class)
class NotificationApplicationTest {

	@Mock private NotificationService nService;
	@Mock private NotificationMapper nMapper;
	@InjectMocks private NotificationApplicationImpl notiApplication;
	
	
	//getNotificationsByAuthUser
	@Test
	void getNotificationsByAuthUser() {
		Page<Notification> pageNotification = Page.empty();
		ResPaginationG<NotificationDto> res = new ResPaginationG<NotificationDto>();
		
		when(nService.getNotificationsByAuthUser(any(PageInfoDto.class))).thenReturn(pageNotification);
		when(nMapper.pageAndPageInfoDtoToResPaginationG(eq(pageNotification), any(PageInfoDto.class))).thenReturn(res);
		
		assertNotNull(notiApplication.getNotificationsByAuthUser(0, 0, null, null));
	}
	
	//deleteNotificationById
	@Test
	void deleteNotificationById() {
		Long notiId =1L;
		Notification notiDeleted = new Notification();
		
		when(nService.deleteNotificationById(notiId)).thenReturn(notiDeleted);
		when(nMapper.notificationToNotificationDtoWithToWho(notiDeleted)).thenReturn(new NotificationDto());
		
		assertNotNull(notiApplication.deleteNotificationById(notiId));
	}
	
	//deleteAllByAuthUser
	@Test
	void deleteAllByAuthUser() {
		notiApplication.deleteAllByAuthUser();
		verify(nService).deleteAllByAuthUser();
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
