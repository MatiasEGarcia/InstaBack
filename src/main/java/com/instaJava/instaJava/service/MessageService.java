package com.instaJava.instaJava.service;

import java.util.List;
import java.util.Set;

import com.instaJava.instaJava.dto.ChatDto;
import com.instaJava.instaJava.dto.MessageDto;
import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.dto.response.ResPaginationG;

public interface MessageService {

	MessageDto create(String message, Long chatId);
	
	ResPaginationG<MessageDto> getMessagesByChat(Long chatId, PageInfoDto pageInfoDto);
	
	ChatDto messagesWatched(Set<String> messageWatchedIds);
	
	/**
	 * Will get messages don't watched by one user in one specific chat.(all messages from the same chat)
	 * @param chatsId
	 * @param username
	 * @return A list of arrays, where each array has as its first value chat id and second value, the count of messages not
	 *  watched. If there is not any message not watched from any chat, it returns an empty list.
	 */
	List<Long[]> getMessagesNotWatchedCountByChatIds(List<Long> chatsId, String username);
	
	void setAllMessagesNotWatchedAsWatchedByChatId(Long chatId);
}
