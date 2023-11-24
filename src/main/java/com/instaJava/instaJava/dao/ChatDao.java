package com.instaJava.instaJava.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.instaJava.instaJava.entity.Chat;

public interface ChatDao extends JpaRepository<Chat, Long>, JpaSpecificationExecutor<Chat>{

	Page<Chat> findByUsersUserId(Long userId, Pageable page);
}
