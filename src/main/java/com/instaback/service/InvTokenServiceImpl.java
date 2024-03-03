package com.instaback.service;


import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.instaback.dao.InvTokenDao;
import com.instaback.entity.InvToken;
import com.instaback.util.MessagesUtils;

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
		if(tokens == null || tokens.isEmpty()) throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null-or-empty"));
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
