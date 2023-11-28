package com.instaJava.instaJava.service;

import org.springframework.data.domain.Page;

import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.entity.Follow;
import com.instaJava.instaJava.entity.Notification;

public interface NotificationService {
	
	/**
	 * Method to save a notification about the follow request of an user to another,
	 * plus doing a websocket message to the user followed , in the case that is
	 * connected, this way will know that it have a follow request.
	 * 
	 * @param follow - follow entity previously saved. (can't be null)
	 * @param customMessage - specific message to this notification, makes this notification more specific. (can't be null)
	 * @return void.
	 */
	void saveNotificationOfFollow(Follow follow, String customMessage);

	/**
	 * Method to get notifications with toWho atribute equal to the auth user.
	 * 
	 * @param pageInfoDto pagination details.
	 * @return Page of Notification.
	 * @throws IllegalArgumentException if pageInfoDto is null, pageInfoDto.sortDir is null or pageInfoDto.sortField is null.
	 */
	Page<Notification> getNotificationsByAuthUser(PageInfoDto pageInfoDto);
	
	/**
	 * Method to delete a notification record by its id.
	 * @param notiId notification id.
	 * @return void.
	 * @throws IllegalArgumentException if notiId is null.
	 * @throws IllegalArgumentException if notification attribute toWho doesn't have the same user information 
	 * than the authenticated user.
	 */
	void deleteNotificationById(Long notiId);
	
	/**
	 * Method to get notification record by id.
	 * @param notiId notification id.
	 * @throws IllegalArgumentException if notiId is null
	 * @throws IllegalArgumentException if there wasn't any record with given id.
	 */
	Notification getNotificationById(Long notiId);
	
}
