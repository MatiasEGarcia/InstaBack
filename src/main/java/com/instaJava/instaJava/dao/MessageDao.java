package com.instaJava.instaJava.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.instaJava.instaJava.entity.Chat;
import com.instaJava.instaJava.entity.Message;

public interface MessageDao extends JpaRepository<Message,Long> {

	Page<Message> findByChatId(Long id, Pageable pageable);
	
	/**
	 * Will count how many messages were not watched from the user(by username) in an specific chat.
	 * @param chatId
	 * @param username
	 * @return List of Arrays, one array will have as first value the chat id and as second value the number of messages not watched.
	 */
	@Query(value="SELECT c.id, COUNT(m.id) FROM Messages AS m JOIN Chats AS c ON m.chat = c.id WHERE c.id IN (:chatsIds) AND m.watched_by NOT LIKE %:username% GROUP BY c.id", nativeQuery = true)
	List<Long[]> countByUserNoWatchedAndChatId(@Param(value = "chatsIds")List<Long> chatsIds,@Param(value = "username")String username);
	
	/**
	 * Will get all messages record which were not watched by a specific user in an specific chat.
	 * @param chatId - chat's id
	 * @param username - user's username.
	 * @return List with all messages-
	 */
	@Query(value="SELECT m FROM Message m JOIN Chat c ON m.chat = c.id WHERE c = :chat AND m.watchedBy NOT LIKE %:username%")
	List<Message> findAllByChatAndUserNoWatched(@Param(value = "chat")Chat chat, @Param(value ="username") String username);
}
