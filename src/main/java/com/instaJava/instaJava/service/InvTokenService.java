package com.instaJava.instaJava.service;

import java.util.List;

import com.instaJava.instaJava.entity.InvToken;

public interface InvTokenService {

	List<InvToken> invalidateTokens(List<String> tokens);
	
	boolean existByToken(String token);
}
