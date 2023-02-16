package com.instaJava.instaJava.service;

public interface InvTokenService {

	void invalidateToken(String token);
	
	void deleteTokensSheduler();
	
	boolean existByToken(String token);
}
