package com.instaJava.instaJava.service;

import java.util.Calendar;

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
	public void invalidateToken(String token) {
		Calendar calendar = Calendar.getInstance();
		invTokenDao.save(InvToken.builder().token(token).invalidateDate(calendar.getTime()).build());
	}

	//When a user logout, it's token is saved in bdd, the token still will be valid for some time
	//so each hour I will delete those tokens already expired
	@Override
	@Transactional
	@Scheduled(cron="0 0 * * * *") //1 hour 
	public void deleteTokensSheduler() {
		Calendar calendar = Calendar.getInstance();
		//token expire after 10min, refresh after 30 min
		calendar.set(Calendar.MINUTE,-35); 
		invTokenDao.deleteByInvalidateDateLessThan(calendar.getTime());
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existByToken(String token) {
		return invTokenDao.existsByToken(token);
	}

}
