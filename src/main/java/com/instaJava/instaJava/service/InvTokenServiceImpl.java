package com.instaJava.instaJava.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.instaJava.instaJava.dao.InvTokenDao;
import com.instaJava.instaJava.entity.InvToken;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class InvTokenServiceImpl implements InvTokenService{

	private final InvTokenDao invTokenDao;
	
	@Override
	@Transactional
	public void invalidateTokens(List<String> tokens) {
		List<InvToken> invTokens = new ArrayList<>();
		LocalDateTime localDateTime = LocalDateTime.now();
		for(int i = 0 ; i< tokens.size() ; i++) {
			invTokens.add(InvToken.builder()
					.invalidateDate(localDateTime)
					.token(tokens.get(i)).
					build());
		}
		invTokenDao.saveAll(invTokens);
	}

	//When a user logout, it's token is saved in bdd, the token still will be valid for some time
	//so each hour I will delete those tokens already expired
	@Override
	@Transactional
	@Scheduled(cron="0 0 * * * *") //1 hour 
	public void deleteTokensSheduler() {
		LocalDateTime localDateTime = LocalDateTime.now();
		//token expire after 10min, refresh after 30 min
		localDateTime.minusMinutes(35);
		invTokenDao.deleteByInvalidateDateLessThan(localDateTime);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existByToken(String token) {
		return invTokenDao.existsByToken(token);
	}

}
