package com.instaback.service;

import java.util.Optional;

import org.springframework.data.domain.Page;

import com.instaback.dto.MessageDto;
import com.instaback.dto.PageInfoDto;
import com.instaback.entity.Chat;
import com.instaback.entity.Comment;
import com.instaback.entity.Follow;
import com.instaback.entity.Notification;
import com.instaback.exception.InvalidActionException;
import com.instaback.exception.RecordNotFoundException;

public interface NotificationService {

	/**
	 * Method to save a notification about the follow request of an user to another,
	 * plus doing a websocket message to the user followed , in the case that is
	 * connected, this way will know that it have a follow request.
	 * 
	 * @param follow        - follow entity previously saved. (can't be null)
	 * @param customMessage - specific message to this notification, makes this
	 *                      notification more specific. (can't be null)
	 * @return void.
	 */
	void saveNotificationOfFollow(Follow follow, String customMessage);

	/**
	 * Method to send a notification when a message was created.
	 * 
	 * @param listUsers  - list of users who send a notification.
	 * @param messageDto - message created.
	 * @throws IllegalArgumentException if some param is null or empty
	 */
	void saveNotificationOfMessage(Chat chat, MessageDto messageDto);

	/**
	 * Method to send a notification when a comment was created.
	 * 
	 * @param comment
	 * @param customMessage - specific message to this notification, makes this
	 *                      notification more specific. (can't be null);
	 * @return void.
	 */
	void saveNotificationOfComment(Comment comment, String customMessage);

	/**
	 * Method to get notifications with toWho atribute equal to the auth user.
	 * 
	 * @param pageInfoDto pagination details.
	 * @return Page with Notifications and pagination info.
	 * @throws IllegalArgumentException if pageInfoDto is null, pageInfoDto.sortDir
	 *                                  is null or pageInfoDto.sortField is null.
	 * @throws RecordNotFoundException  if none notification was found.
	 */
	Page<Notification> getNotificationsByAuthUser(PageInfoDto pageInfoDto);

	/**
	 * Method to delete a notification record by its id.
	 * 
	 * @param notiId notification id.
	 * @return void.
	 * @throws IllegalArgumentException if notiId is null.
	 * @throws InvalidActionException   if notification attribute toWho doesn't have
	 *                                  the same user information than the
	 *                                  authenticated user.
	 */
	Notification deleteNotificationById(Long notiId);

	/**
	 * Delete all auth user's notifications
	 */
	void deleteAllByAuthUser();

	/**
	 * Method to get notification record by id.
	 * 
	 * @param notiId notification id.
	 * @return Optional with notification if exists.
	 * @throws IllegalArgumentException if notiId is null.
	 */
	Optional<Notification> findNotificationById(Long notiId);

}
