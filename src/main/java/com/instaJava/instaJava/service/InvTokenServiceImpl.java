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
		invTokenDao.save(InvToken.builder().token(token).invalidateDate(calendar).build());
	}

	//I want to delete all the tokens that were been for 5minuts in the bdd
	@Override
	@Transactional
	@Scheduled(cron="0 */5 * * * *")
	public void deleteTokensSheduler() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.MINUTE,-5); 
		invTokenDao.deleteAllByInvalidateDate(calendar);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existByToken(String token) {
		return invTokenDao.existsByToken(token);
	}

}
