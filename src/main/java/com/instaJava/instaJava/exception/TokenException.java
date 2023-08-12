package com.instaJava.instaJava.exception;

/**
 * This exception will be throw when a token is invalid, for example, 
 * when the client logout, the client's tokens are invalid in the future.
 * @author matia
 *
 */
public class TokenException extends InvalidException{
	
	private static final long serialVersionUID = 1L;

	public TokenException(Exception e) {
		super(e);
	}
	
	public TokenException(String e) {
		super(e);
	}
	
}
