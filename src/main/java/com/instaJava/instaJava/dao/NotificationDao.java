package com.instaJava.instaJava.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.instaJava.instaJava.entity.Notification;

public interface NotificationDao extends JpaRepository<Notification, Long>, JpaSpecificationExecutor<Notification>{

	Page<Notification> findByToWho(Long toWhoId, Pageable pageable);
}
