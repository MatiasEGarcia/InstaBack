package com.instaJava.instaJava.dao;

import java.util.Calendar;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import com.instaJava.instaJava.entity.InvToken;

public interface InvTokenDao extends JpaRepository<InvToken, Long> {

	@Modifying
	void deleteAllByInvalidateDate(Calendar calendar);

	Boolean existsByToken(String token);
}
