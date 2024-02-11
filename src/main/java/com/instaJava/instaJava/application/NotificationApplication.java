package com.instaJava.instaJava.application;

import org.springframework.data.domain.Sort.Direction;

import com.instaJava.instaJava.dto.NotificationDto;
import com.instaJava.instaJava.dto.response.ResPaginationG;

public interface NotificationApplication {
	
	/**
	 * Method to get notifications with toWho atribute equal to the auth user.
	 * 
	 * @param pageInfoDto pagination details.
	 * @return ResPaginationG object with Notifications and pagination info.
	 */
	ResPaginationG<NotificationDto> getNotificationsByAuthUser(int pageNo, int pageSize, String sortField,Direction sortDir);
	
	/**
	 * Method to delete a notification record by it's id.
	 * 
	 * @param notiId notification id.
	 * @return Notification.
	 */
	NotificationDto deleteNotificationById(Long notiId);
	
	/**
	 * Delete all auth user's notifications
	 */
	void deleteAllByAuthUser();
	
}
