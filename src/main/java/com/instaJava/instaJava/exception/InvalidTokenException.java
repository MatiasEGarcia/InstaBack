package com.instaJava.instaJava.exception;

import org.springframework.http.HttpStatus;

/**
 * This exception will be throw when a token is invalid, for example, 
 * when the client logout, the client's tokens are invalid in the future.
 * @author matia
 *
 */
public class InvalidTokenException extends InvalidException{
	
	private static final long serialVersionUID = 1L;
	
	public InvalidTokenException(String msg,HttpStatus status) {
		super(msg,status);
	}
	
	public InvalidTokenException(String msg,HttpStatus status, Exception e) {
		super(msg,status,e);
	}
}
