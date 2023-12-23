package com.instaJava.instaJava.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.instaJava.instaJava.entity.Chat;

public interface ChatDao extends JpaRepository<Chat, Long>, JpaSpecificationExecutor<Chat>{

	Page<Chat> findByChatUsersUserUserId(Long userId, Pageable page);
	
	
	//Thing to have in consideration, if there are 2 messages with the same sendedAt, can I get 2 chats ,but they are the same
	/**
	 * Get user's chats with last message in that chat, 
	 * order by user last message.
	 * 
	 * @param userId - user's id to search chats.
	 * @param userUsername - user's username to search last message
	 * @param page - pagination info.
	 * @return Page of Chats.
	 */
	@Query(value = "SELECT NEW com.instaJava.instaJava.entity.Chat(c, m.body as lastMessage) "
			+ "FROM Chat c "
			+ "JOIN ChatUser ch "
			+ "ON c.chatId = ch.chat.chatId "
			+ "JOIN Message m "
			+ "ON c.chatId = m.chat.chatId "
			+ "AND m.sendedAt = (SELECT MAX(m2.sendedAt) FROM Message m2 WHERE m2.chat.chatId = c.chatId) "
			+ "WHERE ch.user.userId = :userId "
			+ "ORDER BY (SELECT MAX(m3.sendedAt) FROM Message m3 WHERE m3.chat.chatId = c.chatId AND m3.userOwner = :userOnwer) "
			+ "DESC ")
	Page<Chat> findChatsByUser(@Param(value = "userId")Long userId, @Param(value = "userOnwer")String userUsername, Pageable page);
}
