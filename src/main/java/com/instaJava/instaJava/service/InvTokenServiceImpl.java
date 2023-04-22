package com.instaJava.instaJava.service;


import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.instaJava.instaJava.dao.InvTokenDao;
import com.instaJava.instaJava.entity.InvToken;
import com.instaJava.instaJava.util.MessagesUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InvTokenServiceImpl implements InvTokenService{

	private final Clock clock;
	private final InvTokenDao invTokenDao;
	private final MessagesUtils messUtils;
	
	@Override
	@Transactional
	public List<InvToken> invalidateTokens(List<String> tokens) {
		if(tokens == null || tokens.isEmpty()) throw new IllegalArgumentException(messUtils.getMessage("exception.argument-not-null-empty"));
		List<InvToken> invTokens = new ArrayList<>();
		for(int i = 0 ; i< tokens.size() ; i++) {
			invTokens.add(InvToken.builder()
					.invalidateDate(ZonedDateTime.now(clock))
					.token(tokens.get(i)).
					build());
		}
		return invTokenDao.saveAll(invTokens);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existByToken(String token) {
		return invTokenDao.existsByToken(token);
	}

}
