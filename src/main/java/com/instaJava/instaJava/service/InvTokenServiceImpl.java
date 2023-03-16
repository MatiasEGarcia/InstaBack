package com.instaJava.instaJava.service;


import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.instaJava.instaJava.dao.InvTokenDao;
import com.instaJava.instaJava.entity.InvToken;
import com.instaJava.instaJava.util.MessagesUtils;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class InvTokenServiceImpl implements InvTokenService{

	private final Clock clock;
	private final InvTokenDao invTokenDao;
	private final MessagesUtils messUtils;
	
	@Override
	@Transactional
	public List<InvToken> invalidateTokens(List<String> tokens) {
		if(tokens == null || tokens.isEmpty()) throw new IllegalArgumentException(messUtils.getMessage("exepcion.argument-not-null-empty"));
		List<InvToken> invTokens = new ArrayList<>();
		for(int i = 0 ; i< tokens.size() ; i++) {
			invTokens.add(InvToken.builder()
					.invalidateDate(ZonedDateTime.now(clock))
					.token(tokens.get(i)).
					build());
		}
		return invTokenDao.saveAll(invTokens);
	}

	//When a user logout, it's token is saved in bdd, the token still will be valid for some time
	//so each hour I will delete those tokens already expired
	@Override
	@Transactional
	@Scheduled(cron="0 0 * * * *") //1 hour 
	public void deleteTokensSheduler() {
		Clock clock = Clock.systemUTC();
		ZonedDateTime zonedDateTime = ZonedDateTime.now(clock);
		//token expire after 10min, refresh after 30 min
		invTokenDao.deleteByInvalidateDateLessThan(zonedDateTime.minusMinutes(35));
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existByToken(String token) {
		return invTokenDao.existsByToken(token);
	}

}
