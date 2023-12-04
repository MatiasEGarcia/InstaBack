package com.instaJava.instaJava.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.instaJava.instaJava.entity.Message;

public interface MessageDao extends JpaRepository<Message,Long> {

	Page<Message> findByChatChatId(Long chatId, Pageable pageable);
	
}
