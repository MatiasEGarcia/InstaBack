package com.instaback.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Meta;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.instaback.entity.Notification;
import com.instaback.entity.User;

public interface NotificationDao extends JpaRepository<Notification, Long>, JpaSpecificationExecutor<Notification>{

	Page<Notification> findByToWhoId(Long toWhoId, Pageable pageable);
	
	@Meta(comment = "delete all notifications by who received it")
	@Modifying
	@Query("DELETE FROM Notification n WHERE n.toWho = :user")
	void deleteAllByToWho(@Param(value = "user")User user);
}
