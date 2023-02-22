package com.instaJava.instaJava.service;

import java.util.List;

public interface InvTokenService {

	void invalidateTokens(List<String> tokens);
	
	void deleteTokensSheduler();
	
	boolean existByToken(String token);
}
