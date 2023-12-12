package com.instaJava.instaJava.dao;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;

import com.instaJava.instaJava.entity.ChatUser;

public interface ChatUserDao extends JpaRepository<ChatUser, Long>  {

	List<ChatUser> findByChatChatId(Long chatId);
	
	long deleteByChatChatIdAndUserUsernameIn(Long chatId , Set<String> listUsername);
}
