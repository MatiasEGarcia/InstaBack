package com.instaJava.instaJava.util;

import java.time.Clock;
import java.time.ZonedDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.instaJava.instaJava.dao.InvTokenDao;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DeleteTokensSheduler {

	private final Clock clock;
	private final InvTokenDao invTokenDao;

	/*
	 * When a user logout, it's token is saved in bdd, the token still will be valid
	 * for some time so each hour I will delete those tokens already expired
	 * 
	 */
	@Transactional
	@Scheduled(cron = "0 0 * * * *") // 1 hour
	public void deleteTokensSheduler() {
		ZonedDateTime zonedDateTime = ZonedDateTime.now(clock);
		// token expire after 10min, refresh after 30 min
		invTokenDao.deleteByInvalidateDateLessThan(zonedDateTime.minusMinutes(35));
	}
}
