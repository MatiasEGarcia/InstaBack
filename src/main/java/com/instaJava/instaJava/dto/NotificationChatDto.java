package com.instaJava.instaJava.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * @author matia
 *
 *Notification dto for new messages, chatDto will contain info about the new message's chat and messageDto will contain message 
 *info.
 *
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class NotificationChatDto {//maybe should I change it's name to sokcetChatMessageDto or something like that

	private ChatDto chatDto;
	
	private MessageDto MessageDto;
}
