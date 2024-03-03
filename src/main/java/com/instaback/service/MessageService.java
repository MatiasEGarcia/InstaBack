package com.instaback.service;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;

import com.instaback.dto.PageInfoDto;
import com.instaback.entity.Chat;
import com.instaback.entity.Message;

public interface MessageService {

	/**
	 * To create a Message record.
	 * 
	 * @param message - Message's content.
	 * @param chat    - Chat where the message belongs
	 * @return new message created
	 * @throws IllegalArgumentException if message, chat, or chat.id is null.
	 * @throws InvalidActionException   if message is blank.
	 */
	Message create(String message, Chat chat);

	/**
	 * Get messages by chat.
	 * 
	 * @param chat        - chat from where to get messages
	 * @param pageInfoDto - pagination info
	 * @return Page<Message> with messages and pagination info.
	 * @throws IllegalArgumentException if some param is null.
	 * @throws RecordNotFoundException  no messages were found.
	 */
	Page<Message> getMessagesByChat(Chat chat, PageInfoDto pageInfoDto);

	/**
	 * To set messages as watched by auth user
	 * 
	 * @param messageWatchedIds - list of messages watched's id.
	 * @return number of messages not watched yet.
	 * @throws IllegalArgumentException if messageWatchedIds is null or empty.
	 */
	Long messagesWatched(Set<String> messageWatchedIds);

	/**
	 * Will get messages don't watched by one user in one specific chat.(all
	 * messages from the same chat)
	 * 
	 * @param chatsId
	 * @param username
	 * @return A list of arrays, where each array has as its first value chat id and
	 *         second value, the count of messages not watched. If there is not any
	 *         message not watched from any chat, it returns an empty list.
	 * @throws IllegalArgumentException if some param is null, empty or blank.
	 */
	List<Long[]> getMessagesNotWatchedCountByChatIds(List<Long> chatsId, String username);

	/**
	 * To set all messages as watched by chat.
	 * 
	 * @param chat
	 * @throws IllegalArgumentException if chat is null or chat.id is null.
	 */
	void setAllMessagesNotWatchedAsWatchedByChatId(Chat chat);
}
