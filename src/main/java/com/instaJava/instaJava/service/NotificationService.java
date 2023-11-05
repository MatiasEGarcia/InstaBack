package com.instaJava.instaJava.service;

import org.springframework.data.domain.Page;

import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.entity.Follow;
import com.instaJava.instaJava.entity.Notification;

public interface NotificationService {
	
	void saveNotificationOfFollow(Follow follow, String customMessage);

	Page<Notification> getNotificationsByAuthUser(PageInfoDto pageInfoDto);
	
}
