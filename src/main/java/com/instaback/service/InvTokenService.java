package com.instaback.service;

import java.util.List;

import com.instaback.entity.InvToken;

public interface InvTokenService {

	/**
	 * 
	 * Save tokens needed to be invalidated.
	 * 
	 * @param tokens. Collection of Tokens to invalidate.
	 * @throws IllegalArgumentException if @param tokens is null or empty.
	 * @return Collection of tokens invalidated.
	 */
	List<InvToken> invalidateTokens(List<String> tokens);
	
	/**
	 * Check if token is invalid.
	 * 
	 * @param token - token to check if exists.
	 * @return true if Token record exists,else false.
	 */
	boolean existByToken(String token);
}
