package com.instaJava.instaJava.dto;

import java.io.Serializable;
import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.instaJava.instaJava.enums.NotificationType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * @author matia Notification data transfer object, it doesn't have to who user.
 *         Usually only the user receiver will get his notifications. And he
 *         already knows who is the user in toWho.
 *
 */

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class NotificationDto implements Serializable {

	private static final long serialVersionUID = 1L;

	private String id;

	private NotificationType notificationType;

	// if the notification is from a comment , then this will be the comment id, if
	// the notification is for a new
	// publication ,then this will be the publication id.
	private String elementId;

	private String notiMessage;

	private boolean watched;

	private UserDto toWho;

	private UserDto fromWho;

	@JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
	private ZonedDateTime createdAt;
}
