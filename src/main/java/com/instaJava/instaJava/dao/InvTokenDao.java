package com.instaJava.instaJava.dao;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.instaJava.instaJava.entity.InvToken;

public interface InvTokenDao extends JpaRepository<InvToken, Long> {

	@Modifying
	@Query("DELETE FROM InvToken i WHERE i.invalidateDate < :date")
	void deleteByInvalidateDateLessThan(@Param(value = "date")Date date);

	Boolean existsByToken(String token);
}
