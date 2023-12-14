package com.instaJava.instaJava.dao;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.instaJava.instaJava.entity.ChatUser;

public interface ChatUserDao extends JpaRepository<ChatUser, Long>  {

	List<ChatUser> findByChatChatId(Long chatId);
	
	Optional<ChatUser> findByChatChatIdAndUserUserId(Long chatId, Long userId);
	
	@Modifying
	@Query("DELETE FROM ChatUser ch WHERE ch.chat IN (SELECT c FROM Chat c WHERE c.chatId= :chatId) "
			+ "AND ch.user IN (SELECT u FROM User u where u.username IN (:usernames))")
	void deleteByChatIdAndUserUsernameIn(@Param(value="chatId")Long chatId , @Param(value="usernames")Set<String> listUsername);
}
