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

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class NotificationDto implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String notiId;
	
	private NotificationType notificationType;
	
	private String notiMessage;
	
	private boolean watched;
	
	private UserDto fromWho;
	
	@JsonFormat(shape = Shape.STRING, pattern ="yyyy-MM-dd'T'HH:mm:ss.SSSZ")
	private ZonedDateTime createdAt;
}
