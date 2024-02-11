package com.instaJava.instaJava.application;

import java.util.Set;

import org.springframework.data.domain.Sort.Direction;

import com.instaJava.instaJava.dto.MessageDto;
import com.instaJava.instaJava.dto.request.ReqNewMessage;
import com.instaJava.instaJava.dto.response.ResPaginationG;

public interface MessageApplication{

	/**
	 * Create a message
	 * @param reqNewMessage - new message info.
	 * @return New Message.
	 * @throws IllegalArgumentException if reqNewMessage is null
	 */
	MessageDto create(ReqNewMessage reqNewMessage);

	/**
	 * Get messages by chat id.
	 * @param chatId - chat's id.
	 * @param pageNo.    - For pagination, number of the page.
	 * @param pageSize.  - For pagination, size of the elements in the same page.
	 * @param sortField. - For pagination, sorted by..
	 * @param sortDir.   - In what direction is sorted, asc or desc.
	 * @return ResPaginationG<MessageDto> with list of messages and it's pagination.
	 */
	ResPaginationG<MessageDto> getMessagesByChat(Long chatId, int pageNo, int pageSize, String sortField, Direction sortDir);
	
	/**
	 * To set messages as watched by auth user
	 * @param messageWatchedIds - list of messages watched's id.
	 * @return number of messages not watched yet.
	 */
	Long messagesWatched(Set<String> messageWatchedIds);
	
	/**
	 * Set all messages from a chat id as watched by the authenticated user .
	 * @param chatId - messages' chat.
	 */
	void setAllMessagesNotWatchedAsWatchedByChatId(Long chatId);
	
	

}
